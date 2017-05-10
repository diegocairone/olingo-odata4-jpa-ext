package com.cairone.olingo.ext.jpa.processors;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Builder;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Parameter;
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
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
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
			if(edmAction != null) {
				String operationName = edmAction.name().isEmpty() ? operation.getClass().getSimpleName() : edmAction.name();
				operationsMap.put(operationName, operation);
			}
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
		processActionEntity(request, response, uriInfo, requestFormat, null);
	}

	@Override
	public void processActionEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceAction uriResourceAction = (UriResourceAction) resourcePaths.get(resourcePaths.size() - 1);
		org.apache.olingo.commons.api.edm.EdmAction action = uriResourceAction.getAction();
		
		SelectOption selectOption = uriInfo.getSelectOption();
	    ExpandOption expandOption = uriInfo.getExpandOption();
	    CountOption countOption = uriInfo.getCountOption();

		String operationName = action.getName();
		Operation<?> operation = operationsMap.get(operationName);
		
		Map<String, Parameter> parameters = readActionParameters(action, request.getBody(), requestFormat);
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
	    	
	    	String returnedEntitySetName = action.getReturnType() == null ? null : entityTypeMap.get(action.getReturnType().getType().getName());
	    	edmEntitySet = returnedEntitySetName == null ? null : serviceMetadata.getEdm().getEntityContainer().getEntitySet(returnedEntitySetName);
	    	
	    } else {
	    	edmEntitySet = uriResourceAction.getActionImport().getReturnedEntitySet();
	    }
	    		
		EdmEntityType edmEntityType = edmEntitySet == null ? null : edmEntitySet.getEntityType();
	    	    
	    String selectList = edmEntityType == null ? "" : odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption);
	    boolean count = countOption == null ? false : countOption.getValue();
	    
		EntityCollection entityCollection = new EntityCollection();
		List<Entity> result = entityCollection.getEntities();
			    
		try {
			Object object = operation.doOperation(action.isBound(), keyPredicateMap);

			if(Collection.class.isAssignableFrom(object.getClass())) {
				for(Class<?> clazzIFace : object.getClass().getInterfaces()) {
					if(List.class.isAssignableFrom(clazzIFace)) {
						
						@SuppressWarnings("unchecked")
						Collection<Object> collection = (Collection<Object>) object;
						int nroItems = 0;
						
						for(Object item : collection) {
							Entity entity = writeEntity(item, expandOption);
							result.add(entity);
							nroItems++;
						}
						
						if(count) entityCollection.setCount(nroItems);
					}
				}
			} else {
				Entity entity = writeEntity(object, expandOption);
				result.add(entity);
			}
			
		} catch (ODataException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}

		ODataSerializer serializer = odata.createSerializer(responseFormat);
	    ContextURL contextUrl = null;
	    
		try {
			Builder builder = ContextURL.with().serviceRoot(new URI(SERVICE_ROOT));
			if(edmEntitySet != null) builder.entitySet(edmEntitySet);
			if(selectList != null) builder.selectList(selectList);
			
			contextUrl = builder.build();
			
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}

		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();

		EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
			.id(id)
			.contextURL(contextUrl)
			.count(countOption)
			.select(selectOption)
			.expand(expandOption).build();
		
		SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entityCollection, opts);
		InputStream serializedContent = serializerResult.getContent();

		response.setContent(serializedContent);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@Override
	public void processActionEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceAction uriResourceAction = (UriResourceAction) resourcePaths.get(resourcePaths.size() - 1);
		org.apache.olingo.commons.api.edm.EdmAction action = uriResourceAction.getAction();
		
		String operationName = action.getName();
		Operation<?> operation = operationsMap.get(operationName);
		
		Map<String, Parameter> parameters = readActionParameters(action, request.getBody(), requestFormat);
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
	    	
	    	String returnedEntitySetName = action.getReturnType() == null ? null : entityTypeMap.get(action.getReturnType().getType().getName());
	    	edmEntitySet = returnedEntitySetName == null ? null : serviceMetadata.getEdm().getEntityContainer().getEntitySet(returnedEntitySetName);
	    	
	    } else {
	    	edmEntitySet = uriResourceAction.getActionImport().getReturnedEntitySet();
	    }
	    		
		EdmEntityType edmEntityType = edmEntitySet == null ? null : edmEntitySet.getEntityType();
		
		SelectOption selectOption = uriInfo.getSelectOption();
	    ExpandOption expandOption = uriInfo.getExpandOption();
	    
	    String selectList = edmEntityType == null ? "" : odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption);
	    
		Entity entity;
	    
		try {
			Object object = operation.doOperation(action.isBound(), keyPredicateMap);
			entity = writeEntity(object, expandOption); 
			
		} catch (ODataException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}

	    ContextURL contextUrl = null;
		try {
			Builder builder = ContextURL.with()
					.serviceRoot(new URI(SERVICE_ROOT))
					.suffix(Suffix.ENTITY);
			
			if(edmEntitySet != null) builder.entitySet(edmEntitySet);
			if(selectList != null) builder.selectList(selectList);
			
			contextUrl = builder.build();
			
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
	    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).select(selectOption).expand(expandOption).build();
	    
	    if(responseFormat == null) {
	    	response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	    	return;
	    }
	    
	    ODataSerializer serializer = odata.createSerializer(responseFormat);
	    SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, options);
	    InputStream entityStream = serializerResult.getContent();

	    response.setContent(entityStream);
	    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}
}
