package com.cairone.olingo.ext.jpa.processors;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.core.uri.queryoption.TopOptionImpl;
import org.springframework.context.ApplicationContext;

import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmFunction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.cairone.olingo.ext.jpa.utilities.Util;
import com.google.common.collect.Iterables;

public class EntitySetProcessor extends BaseProcessor implements EntityProcessor, EntityCollectionProcessor {
	
	protected Map<String, DataSource> dataSourceMap = new HashMap<>();
	protected Map<String, Operation<?>> operationsMap = new HashMap<>();
	protected Integer maxTopOption = null;
	
	public EntitySetProcessor initialize(ApplicationContext context) throws ODataApplicationException {
		super.initialize(context);
		
		context.getBeansOfType(DataSource.class).entrySet()
			.stream()
			.forEach(entry -> {
				DataSource dataSource = entry.getValue();
				dataSourceMap.put(dataSource.isSuitableFor(), dataSource);
			});
		
		context.getBeansOfType(Operation.class).entrySet()
			.stream()
			.forEach(entry -> {
				Operation<?> operation = entry.getValue();
				EdmFunction edmFunction = operation.getClass().getAnnotation(EdmFunction.class);
				if(edmFunction != null) {
					String operationName = edmFunction.name().isEmpty() ? operation.getClass().getSimpleName() : edmFunction.name();
					operationsMap.put(operationName, operation);
				}
			});
		
		return this;
	}
	
	public EntitySetProcessor setMaxTopOption(Integer maxTopOption) {
		this.maxTopOption = maxTopOption;
		return this;
	}

	@Override
	public EntitySetProcessor setServiceRoot(String ServiceRoot) {
		super.setServiceRoot(ServiceRoot);
		return this;
	}

	@Override
	public EntitySetProcessor setDefaultEdmPackage(String DefaultEdmPackage) {
		super.setDefaultEdmPackage(DefaultEdmPackage);
		return this;
	}

	@Override
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		
		DataSource dataSource = dataSourceMap.get(edmEntitySet.getName());
		
		if(dataSource == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();
		
		Class<?> clazz = entitySetMap.get(edmEntitySet.getName());
		Object object = null;
		
    	EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
    	String[] keys = edmEntity.key();
    	Map<String, Object> keyValues = Arrays.asList(keys)
    		.stream()
    		.collect(Collectors.toMap(x -> x, x -> x));
    	
    	List<Link> navLinks = requestEntity.getNavigationLinks();
    	
    	for(Link navlink : navLinks) {
    		if(navlink.getInlineEntity() != null) {
    			Entity entity = navlink.getInlineEntity();
    			writeNavLinksFromNavBindings(entity, dataSourceMap, request.getRawBaseUri());
    		} else if (navlink.getInlineEntitySet() != null) {
    			EntityCollection entityCollection = navlink.getInlineEntitySet();
    			for(Entity entity : entityCollection) {
    				writeNavLinksFromNavBindings(entity, dataSourceMap, request.getRawBaseUri());
    			}
    		}
    	}
    	
    	writeNavLinksFromNavBindings(requestEntity, dataSourceMap, request.getRawBaseUri());
    	
    	try {
    		object = writeObject(clazz, requestEntity);
    		
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
    	
		Entity createdEntity = new Entity();
		Object createdObject = dataSource.create(object);
		
		try
		{
	    	for(Field fld : createdObject.getClass().getDeclaredFields()) {
	    		
	    		com.cairone.olingo.ext.jpa.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
				
	            if (edmProperty != null) {
	            	
	            	fld.setAccessible(true);
	            	
	            	String name = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
	            	Object value = fld.get(createdObject);
	            	
	            	if(value != null) {
		            		
		            	if(value instanceof LocalDate) {
		            		
		            		LocalDate localDateValue = (LocalDate) value;
		            		createdEntity.addProperty(new Property(null, name, ValueType.PRIMITIVE, GregorianCalendar.from(localDateValue.atStartOfDay(ZoneId.systemDefault()))));
		            	
		            	} else if(value.getClass().isEnum()) {
		            		
		            		Class<?> fldClazz = fld.getType();
		            		Method getValor = fldClazz.getMethod("getValor");
	    					Enum<?>[] enums = (Enum<?>[]) fldClazz.getEnumConstants();
	    					
	    					Object rvValue = getValor.invoke(value);
	    					
	    					for(Enum<?> enumeration : enums) {
	    						Object rv = getValor.invoke(enumeration);
	    						if(rvValue.equals(rv)) {
		    						createdEntity.addProperty(new Property(null, name, ValueType.ENUM, rv));
		                    		break;
	    						}
	    					}
		            	} else {
		            		createdEntity.addProperty(new Property(null, name, ValueType.PRIMITIVE, value));
		            	}
	            	}
	            	
	            	if(keyValues.containsKey(name)) {
	            		keyValues.put(name, value);
	            	}
	            }
	    	}
		} catch (Exception e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		String entityID = keyValues.entrySet().stream().map(Entry::toString).collect(Collectors.joining(",", "(", ")"));
		try {
			createdEntity.setId(new URI(edmEntitySet.getName() + entityID));
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
	    ContextURL contextUrl = null;
		try {
			contextUrl = ContextURL.with()
					.serviceRoot(new URI(SERVICE_ROOT))
					.entitySet(edmEntitySet)
					.suffix(Suffix.ENTITY)
					.build();
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
		EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
		
		ODataSerializer serializer = this.odata.createSerializer(responseFormat);
		SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

		final String location = request.getRawBaseUri() + '/' + odata.createUriHelper().buildCanonicalURL(edmEntitySet, createdEntity);
		
		response.setContent(serializedResponse.getContent());
		response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		response.setHeader(HttpHeader.LOCATION, location);
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		DataSource dataSource = dataSourceMap.get(edmEntitySet.getName());
		
		if(dataSource == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();

		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		Map<String, UriParameter> keyPredicateMap = keyPredicates
				.stream()
				.collect(Collectors.toMap(UriParameter::getName, x -> x));
		
		List<String> propertiesInJSON = Stream.concat(
				requestEntity.getProperties().stream().map(Property::getName), 
				requestEntity.getNavigationLinks().stream().map(Link::getTitle))
			.collect(Collectors.toList());
		
		Class<?> clazz = entitySetMap.get(edmEntitySet.getName());
		Object object;

		writeNavLinksFromNavBindings(requestEntity, dataSourceMap, request.getRawBaseUri());
		
    	try {
	    	object = writeObject(clazz, requestEntity);
    		
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
    	
    	dataSource.update(keyPredicateMap, object, propertiesInJSON, request.getMethod().equals(HttpMethod.PUT));
    	response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		  
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		
		DataSource dataSource = dataSourceMap.get(edmEntitySet.getName());
		
		if(dataSource == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		Map<String, UriParameter> keyPredicateMap = keyPredicates
				.stream()
				.collect(Collectors.toMap(UriParameter::getName, x -> x));
		
    	dataSource.delete(keyPredicateMap);
    	response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		
	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

	    SelectOption selectOption = uriInfo.getSelectOption();
	    ExpandOption expandOption = uriInfo.getExpandOption();
	    
	    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
	    String selectList;
		try {
			selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption);
		} catch (SerializerException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
	    
		DataSource dataSource = dataSourceMap.get(edmEntitySet.getName());
		
		if(dataSource == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
	    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
	    Map<String, UriParameter> keyPredicateMap = keyPredicates
				.stream()
				.collect(Collectors.toMap(UriParameter::getName, x -> x));
		
	    Entity entity;
	    Object object = dataSource.readFromKey(keyPredicateMap, expandOption, selectOption);
		
		if(object == null) {
			throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
		
		try {	
			entity = writeEntity(object, expandOption);
			
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
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
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
	    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).select(selectOption).expand(expandOption).build();
	    
	    ODataSerializer serializer;
	    SerializerResult serializerResult;
	    
		try {
			serializer = odata.createSerializer(responseFormat);
			serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, options);
		} catch (SerializerException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
	    InputStream entityStream = serializerResult.getContent();

	    response.setContent(entityStream);
	    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}
	
	@Override
	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		final UriResource lastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 1 );
		
		if(lastResourceSegment instanceof UriResourceFunction) {
			readFunctionImportCollection(request, response, uriInfo, responseFormat);
		} else if(lastResourceSegment instanceof UriResourceEntitySet) {
			readEntityCollectionInternal(request, response, uriInfo, responseFormat);
		} else {
			throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}
	}
	
	private void readFunctionImportCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceFunction uriResourceFunction = (UriResourceFunction) resourcePaths.get( uriInfo.getUriResourceParts().size() - 1 );
		org.apache.olingo.commons.api.edm.EdmFunction function = uriResourceFunction.getFunction();
		
		String operationName = function.getName();
		Operation<?> operation = operationsMap.get(operationName);
		
		Map<String, UriParameter> functionParameters = uriResourceFunction.getParameters()
			.stream()
			.collect(Collectors.toMap(UriParameter::getName, x -> x ));
		
		Class<?> clazz = operation.getClass();
		
		for (Field fld : clazz.getDeclaredFields()) {

			EdmParameter edmParameter = fld.getAnnotation(EdmParameter.class);
			if(edmParameter != null) {

				String parameterName = edmParameter.name().isEmpty() ? fld.getName() : edmParameter.name();
				String edmType = edmParameter.type().isEmpty() ? Util.inferEdmType(fld) : edmParameter.type();
				UriParameter parameter = functionParameters.get(parameterName);
				Object value = convertEdmType(edmType, parameter.getText());
				
				fld.setAccessible(true);
				try {
					fld.set(operation, value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
		}
		
		ExpandOption expandOption = uriInfo.getExpandOption();
		
		Map<String, UriParameter> keyPredicateMap = null;
		EdmEntitySet edmEntitySet = null;
		
		if(function.isBound()) {
			
			UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
			List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
			keyPredicateMap = keyPredicates.stream().collect(Collectors.toMap(UriParameter::getName, x -> x));
			
	    	String returnedEntitySetName = entityTypeMap.get(function.getReturnType().getType().getName());
	    	edmEntitySet = serviceMetadata.getEdm().getEntityContainer().getEntitySet(returnedEntitySetName);
		} else {
			edmEntitySet = uriResourceFunction.getFunctionImport().getReturnedEntitySet();
		}
		
		EntityCollection entityCollection = new EntityCollection();
		List<Entity> result = entityCollection.getEntities();
		
		try {
			Object object = operation.doOperation(function.isBound(), keyPredicateMap);
			
			if(Collection.class.isAssignableFrom(object.getClass())) {
				for(Class<?> clazzIFace : object.getClass().getInterfaces()) {
					if(List.class.isAssignableFrom(clazzIFace)) {
						
						@SuppressWarnings("unchecked")
						Collection<Object> collection = (Collection<Object>) object;
						
						for(Object item : collection) {
							Entity entity = writeEntity(item, expandOption);
							result.add(entity);
						}						
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
			contextUrl = ContextURL.with()
					.serviceRoot(new URI(SERVICE_ROOT))
					.entitySet(edmEntitySet)
					.build();
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
		
		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		final EdmEntityType edmEntityType = (EdmEntityType) uriResourceFunction.getFunction().getReturnType().getType();
		
		EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
			.id(id)
			.contextURL(contextUrl)
			.expand(expandOption).build();
		
		SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entityCollection, opts);
		InputStream serializedContent = serializerResult.getContent();

		response.setContent(serializedContent);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}
	
	private void readEntityCollectionInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

	    SelectOption selectOption = uriInfo.getSelectOption();
	    ExpandOption expandOption = uriInfo.getExpandOption();
	    CountOption countOption = uriInfo.getCountOption();
	    SkipOption skipOption = uriInfo.getSkipOption();
	    TopOption topOption = uriInfo.getTopOption();
	    OrderByOption orderByOption = uriInfo.getOrderByOption();
	    FilterOption filterOption = uriInfo.getFilterOption();
	    
	    if(topOption == null && maxTopOption != null) {
	    	topOption = new TopOptionImpl().setValue(maxTopOption);
	    } else if(topOption != null && maxTopOption != null && topOption.getValue() > maxTopOption) {
	    	TopOptionImpl topOptionImpl = (TopOptionImpl) topOption;
	    	topOption = topOptionImpl.setValue(maxTopOption);
	    }
	    
	    String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption);
	    boolean count = countOption == null ? false : countOption.getValue();
	    
		DataSource dataSource = dataSourceMap.get(edmEntitySet.getName());
		
		if(dataSource == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		EntityCollection entityCollection = new EntityCollection();
		List<Entity> result = entityCollection.getEntities();
		
		Iterable<?> data = dataSource.readAll(expandOption, filterOption, orderByOption);
		
		if(count) entityCollection.setCount(Iterables.size(data));
		
		if(skipOption != null) {
			data = Iterables.skip(data, skipOption.getValue());
		}
		
		if(topOption != null) {
			data = Iterables.limit(data, topOption.getValue()); 
		}
		
		try {			
			for(Object object : data) {
				Entity entity = writeEntity(object, expandOption);
				result.add(entity);
			}
		} catch (Exception e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		
		ODataSerializer serializer = odata.createSerializer(responseFormat);

	    ContextURL contextUrl = null;
		try {
			contextUrl = ContextURL.with()
					.serviceRoot(new URI(SERVICE_ROOT))
					.entitySet(edmEntitySet)
					.selectList(selectList)
					.build();
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
}
