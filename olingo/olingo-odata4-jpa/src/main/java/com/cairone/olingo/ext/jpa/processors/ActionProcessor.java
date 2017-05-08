package com.cairone.olingo.ext.jpa.processors;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.ActionEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.ActionEntityProcessor;
import org.apache.olingo.server.api.processor.ActionVoidProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.context.ApplicationContext;

import com.cairone.olingo.ext.jpa.annotations.EdmAction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.interfaces.Operation;

public class ActionProcessor extends BaseProcessor implements ActionEntityProcessor, ActionEntityCollectionProcessor, ActionVoidProcessor {

	private Map<String, Operation<?>> operationsMap = new HashMap<>();
	
	@Override
	public ActionProcessor initialize(ApplicationContext context) throws ODataApplicationException {
		super.initialize(context);
		
		context.getBeansOfType(Operation.class).entrySet()
		.stream()
		.forEach(entry -> {
			Operation<?> operation = entry.getValue();
			EdmAction edmAction = operation.getClass().getAnnotation(EdmAction.class);
			String operationName = edmAction.name().isEmpty() ? operation.getClass().getSimpleName() : edmAction.name();
			operationsMap.put(operationName, operation);
		});
		
		return this;
	}

	@Override
	public ActionProcessor setServiceRoot(String ServiceRoot) {
		super.setServiceRoot(ServiceRoot);
		return this;
	}

	@Override
	public ActionProcessor setDefaultEdmPackage(String DefaultEdmPackage) {
		super.setDefaultEdmPackage(DefaultEdmPackage);
		return this;
	}

	@Override
	public void processActionVoid(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("PROCESS ACTION VOID NOT IMPLEMENTED", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void processActionEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("PROCESS ACTION ENTITY COLLECTION NOT IMPLEMENTED", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void processActionEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceAction uriResourceAction = (UriResourceAction) resourcePaths.get(resourcePaths.size() - 1);
		org.apache.olingo.commons.api.edm.EdmAction action = uriResourceAction.getAction();
		
		String operationName = action.getName();
		Operation<?> operation = operationsMap.get(operationName);
		
		Map<String, Parameter> parameters = readParameters(action, request.getBody(), requestFormat);
		Class<?> clazz = operation.getClass();
		
		for (Field fld : clazz.getDeclaredFields()) {
			
			EdmParameter edmParameter = fld.getAnnotation(EdmParameter.class);
			if(edmParameter != null) {
				
				String parameterName = edmParameter.name().isEmpty() ? fld.getName() : edmParameter.name();
				Parameter parameter = parameters.get(parameterName);
				
				fld.setAccessible(true);
	    		try {
	    			if(fld.getType().isAssignableFrom(LocalDate.class) && parameter.getValue() instanceof GregorianCalendar) {
	    				GregorianCalendar cal = (GregorianCalendar) parameter.getValue();
                		fld.set(operation, cal.toZonedDateTime().toLocalDate());
	    			} else {
	    				fld.set(operation, parameter.getValue());
	    			}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
		}

		Map<String, UriParameter> keyPredicateMap = null;
		EdmEntitySet edmEntitySet = null;
		
	    if(action.isBound()) {
	    	
	    	UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    	List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
	    	keyPredicateMap = keyPredicates.stream().collect(Collectors.toMap(UriParameter::getName, x -> x));
	    	
	    	String returnedEntitySetName = entityTypeMap.get(action.getReturnType().getType().getName());
	    	edmEntitySet = serviceMetadata.getEdm().getEntityContainer().getEntitySet(returnedEntitySetName);
	    	
	    } else {
	    	edmEntitySet = uriResourceAction.getActionImport().getReturnedEntitySet();
	    }
	    		
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		
		SelectOption selectOption = uriInfo.getSelectOption();
	    ExpandOption expandOption = uriInfo.getExpandOption();
	    
	    String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption);
	    
		Entity entity;
	    
		try {
			Object object = operation.doOperation(action.isBound(), keyPredicateMap);
			entity = writeEntity(object, expandOption); 
			
		} catch (ODataException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}

	    ContextURL contextUrl = null;
		try {
			contextUrl = ContextURL.with()
					.serviceRoot(new URI(SERVICE_ROOT))
					.entitySet(edmEntitySet)
					.selectList(selectList)
					.suffix(Suffix.ENTITY)
					.build();
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
	    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).select(selectOption).expand(expandOption).build();
	    
	    ODataSerializer serializer = odata.createSerializer(responseFormat);
	    SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, options);
	    InputStream entityStream = serializerResult.getContent();

	    response.setContent(entityStream);
	    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}
}
