package com.cairone.olingo.ext.jpa.processors;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
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
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.core.uri.queryoption.TopOptionImpl;
import org.springframework.context.ApplicationContext;

import com.cairone.olingo.ext.jpa.annotations.EdmEnum;
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

		final UriResource lastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 1 );

		if(lastResourceSegment instanceof UriResourceEntitySet) {
			createEntityInternal(request, response, uriInfo, requestFormat, responseFormat);
		} else if(lastResourceSegment instanceof UriResourceNavigation) {
			createEntityNavigation(request, response, uriInfo, requestFormat, responseFormat);
		} else {
			throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}
	}
	
	private void createEntityNavigation(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		
		if(resourcePaths.size() != 2) {
			throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
		}
		
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePaths.get(1);
		
		LOG.debug("EntitySet [First segment]: {}", uriResourceEntitySet);
		LOG.debug("NavProperty [Second segment]: {}", uriResourceNavigation);
		
		Object parentobject = readFromEntitySet(uriResourceEntitySet);

		if(parentobject == null) {
			throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
		
		LOG.debug("EDM found: {}", parentobject);

		// *** SECOND SEGMENT
		
		EdmEntitySet edmEntitySet = getNavigationTargetEntitySet(uriResourceEntitySet.getEntitySet(), uriResourceNavigation.getProperty());
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
    	Entity createdEntity;
    	
    	try {
    		object = writeObject(clazz, requestEntity);
    		
    		Object createdObject = dataSource.create(object, parentobject);
    		createdEntity = writeEntity(createdObject, null);
    		
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
    		LOG.error(e.getMessage(), e);
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
			LOG.error(e.getMessage(), e);
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

	private void createEntityInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
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
    	Entity createdEntity;
    	
    	try {
    		object = writeObject(clazz, requestEntity);
    		
    		Object createdObject = dataSource.create(object, null);
    		createdEntity = writeEntity(createdObject, null);
    		
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
    		LOG.error(e.getMessage(), e);
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
			LOG.error(e.getMessage(), e);
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

		final UriResource lastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 1 );

		if(lastResourceSegment instanceof UriResourceEntitySet) {
			updateEntityInternal(request, response, uriInfo, requestFormat, responseFormat);
		} else if(lastResourceSegment instanceof UriResourceNavigation) {
			updateEntityNavigation(request, response, uriInfo, requestFormat, responseFormat);
		} else {
			throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}
	}
	
	private void updateEntityNavigation(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		
		if(resourcePaths.size() != 2) {
			throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
		}
		
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePaths.get(1);
		
		LOG.debug("EntitySet [First segment]: {}", uriResourceEntitySet);
		LOG.debug("NavProperty [Second segment]: {}", uriResourceNavigation);
		
		Object parentobject = readFromEntitySet(uriResourceEntitySet);

		if(parentobject == null) {
			throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
		
		LOG.debug("EDM found: {}", parentobject);

		// *** SECOND SEGMENT

		EdmEntitySet edmEntitySet = getNavigationTargetEntitySet(uriResourceEntitySet.getEntitySet(), uriResourceNavigation.getProperty());
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

		List<UriParameter> keyPredicates = uriResourceNavigation.getKeyPredicates();
		Map<String, UriParameter> keyPredicateMap = keyPredicates
				.stream()
				.collect(Collectors.toMap(UriParameter::getName, x -> x));
		
		List<String> propertiesInJSON = new ArrayList<>();
		
		requestEntity.getProperties().forEach(property -> {
			if(property.getValueType().equals(ValueType.COMPLEX)) {
				ComplexValue complexValue = (ComplexValue) property.getValue();
				complexValue.getValue().forEach(complexProperty -> {
					propertiesInJSON.add(String.format("%s/%s", property.getName(), complexProperty.getName()));
				});
			} else {
				propertiesInJSON.add(property.getName());
			}
		});
		propertiesInJSON.addAll(requestEntity.getNavigationLinks().stream().map(Link::getTitle).collect(Collectors.toList()));
		
		Class<?> clazz = entitySetMap.get(edmEntitySet.getName());
		Object object;

		writeNavLinksFromNavBindings(requestEntity, dataSourceMap, request.getRawBaseUri());
		
    	try {
	    	object = writeObject(clazz, requestEntity);
    		
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
    		LOG.error(e.getMessage(), e);
    		throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
    	
    	dataSource.update(keyPredicateMap, object, parentobject, propertiesInJSON, request.getMethod().equals(HttpMethod.PUT));
    	response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}
	
	private void updateEntityInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
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
		
		List<String> propertiesInJSON = new ArrayList<>();
		
		requestEntity.getProperties().forEach(property -> {
			if(property.getValueType().equals(ValueType.COMPLEX)) {
				ComplexValue complexValue = (ComplexValue) property.getValue();
				complexValue.getValue().forEach(complexProperty -> {
					propertiesInJSON.add(String.format("%s/%s", property.getName(), complexProperty.getName()));
				});
			} else {
				propertiesInJSON.add(property.getName());
			}
		});
		propertiesInJSON.addAll(requestEntity.getNavigationLinks().stream().map(Link::getTitle).collect(Collectors.toList()));
		
		Class<?> clazz = entitySetMap.get(edmEntitySet.getName());
		Object object;

		writeNavLinksFromNavBindings(requestEntity, dataSourceMap, request.getRawBaseUri());
		
    	try {
	    	object = writeObject(clazz, requestEntity);
    		
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
    		LOG.error(e.getMessage(), e);
    		throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
    	
    	dataSource.update(keyPredicateMap, object, null, propertiesInJSON, request.getMethod().equals(HttpMethod.PUT));
    	response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}
	
	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

		final UriResource lastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 1 );

		if(lastResourceSegment instanceof UriResourceEntitySet) {
			deleteEntityInternal(request, response, uriInfo);
		} else if(lastResourceSegment instanceof UriResourceNavigation) {
			deleteEntityNavigation(request, response, uriInfo);
		} else {
			throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}
	}
	
	private void deleteEntityNavigation(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		
		if(resourcePaths.size() != 2) {
			throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
		}
		
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePaths.get(1);
		
		LOG.debug("EntitySet [First segment]: {}", uriResourceEntitySet);
		LOG.debug("NavProperty [Second segment]: {}", uriResourceNavigation);
		
		Object parentobject = readFromEntitySet(uriResourceEntitySet);

		if(parentobject == null) {
			throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
		
		LOG.debug("EDM found: {}", parentobject);

		// *** SECOND SEGMENT

		EdmEntitySet edmEntitySet = getNavigationTargetEntitySet(uriResourceEntitySet.getEntitySet(), uriResourceNavigation.getProperty());

		DataSource dataSource = dataSourceMap.get(edmEntitySet.getName());
		
		if(dataSource == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		List<UriParameter> keyPredicates = uriResourceNavigation.getKeyPredicates();
		Map<String, UriParameter> keyPredicateMap = keyPredicates
				.stream()
				.collect(Collectors.toMap(UriParameter::getName, x -> x));
		
    	dataSource.delete(keyPredicateMap, parentobject);
    	response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	private void deleteEntityInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

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
		
    	dataSource.delete(keyPredicateMap, null);
    	response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}
	
	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		final UriResource lastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 1 );

		if(lastResourceSegment instanceof UriResourceFunction) {
			readFunctionImport(request, response, uriInfo, responseFormat);
		} else if(lastResourceSegment instanceof UriResourceEntitySet) {
			readEntityInternal(request, response, uriInfo, responseFormat);
		} else if(lastResourceSegment instanceof UriResourceNavigation) {
			readNavigationEntityInternal(request, response, uriInfo, responseFormat);
		} else {
			throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}
	}

	private void readEntityInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
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
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
	    
		Entity entity;
		Object object = readFromEntitySet(selectOption, expandOption, uriResourceEntitySet);
		
		if(object == null) {
			throw new ODataApplicationException("Requested entity does not exist", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
		
		try {	
			entity = writeEntity(object, expandOption);
			
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
			LOG.error(e.getMessage(), e);
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
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
	    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).select(selectOption).expand(expandOption).build();
	    
	    ODataSerializer serializer;
	    SerializerResult serializerResult;
	    
		try {
			serializer = odata.createSerializer(responseFormat);
			serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, options);
		} catch (SerializerException e) {
			LOG.error(e.getMessage(), e);
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
			readFunctionImport(request, response, uriInfo, responseFormat);
		} else if(lastResourceSegment instanceof UriResourceEntitySet) {
			readEntityCollectionInternal(request, response, uriInfo, responseFormat);
		} else if(lastResourceSegment instanceof UriResourceNavigation) {
			readNavigationCollectionInternal(request, response, uriInfo, responseFormat);
		} else {
			LOG.warn("NO IMPLEMENTATION FOR {}", lastResourceSegment.getClass());
			throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}
	}
	
	private void readNavigationCollectionInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		
		if(resourcePaths.size() != 2) {
			throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
		}
		
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePaths.get(1);
		
		LOG.debug("EntitySet [First segment]: {}", uriResourceEntitySet);
		LOG.debug("NavProperty [Second segment]: {}", uriResourceNavigation);
		
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		
	    DataSource dsEntitySet = dataSourceMap.get(edmEntitySet.getName());
		
		if(dsEntitySet == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
	    Map<String, UriParameter> keyPredicateMap = keyPredicates
				.stream()
				.collect(Collectors.toMap(UriParameter::getName, x -> x));
		
	    Object parentobject = dsEntitySet.readFromKey(keyPredicateMap, null, null, null);
		
		if(parentobject == null) {
			throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
		
		LOG.debug("EDM found: {}", parentobject);
		
		// *** SECOND SEGMENT
		
		EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
		EdmEntitySet responseEdmEntitySet  = getNavigationTargetEntitySet(edmEntitySet, edmNavigationProperty);
		
		EdmEntityType edmEntityType = responseEdmEntitySet.getEntityType();
		

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
	    
		DataSource dsSecondSegment = dataSourceMap.get(responseEdmEntitySet.getName());
		
		if(dsSecondSegment == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		EntityCollection entityCollection = new EntityCollection();
		List<Entity> result = entityCollection.getEntities();

		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		String nextLink = id + "?$count=true&$skip=";
		
		Iterable<?> data = dsSecondSegment.readAll(expandOption, filterOption, orderByOption, parentobject);
		
		if(count) entityCollection.setCount(Iterables.size(data));
		
		if(skipOption != null) {
			data = Iterables.skip(data, skipOption.getValue());
			nextLink += (skipOption.getValue() + maxTopOption);
		} else {
			nextLink += maxTopOption;
		}
		
		if(topOption != null) {
			if(Iterables.size(data) <= topOption.getValue()) {
				nextLink = null;
			}
			data = Iterables.limit(data, topOption.getValue()); 
		}
		
		try {			
			for(Object targetObject : data) {
				Entity entity = writeEntity(targetObject, expandOption);
				result.add(entity);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		
		ODataSerializer serializer = odata.createSerializer(responseFormat);

	    ContextURL contextUrl = null;
		try {
			if(nextLink != null) {
				LOG.debug("NEXT LINK: {}", nextLink);
				entityCollection.setNext(new URI(nextLink));
			}
			
			contextUrl = ContextURL.with()
					.serviceRoot(new URI(SERVICE_ROOT))
					.entitySet(edmEntitySet)
					.selectList(selectList)
					.build();
		} catch (URISyntaxException e) {
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
		
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
	
	private void readNavigationEntityInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		
		if(resourcePaths.size() != 2) {
			throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
		}
		
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePaths.get(1);
		
		LOG.debug("EntitySet [First segment]: {}", uriResourceEntitySet);
		LOG.debug("NavProperty [Second segment]: {}", uriResourceNavigation);
		
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		Object parentobject = readFromEntitySet(uriResourceEntitySet);
		
		if(parentobject == null) {
			throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}

		
		// *** SECOND SEGMENT
		
		EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
		EdmEntitySet responseEdmEntitySet  = getNavigationTargetEntitySet(edmEntitySet, edmNavigationProperty);

	    SelectOption selectOption = uriInfo.getSelectOption();
	    ExpandOption expandOption = uriInfo.getExpandOption();
	    
	    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
	    String selectList;
		try {
			selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption);
		} catch (SerializerException e) {
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
	    
	    DataSource responseDataSource = dataSourceMap.get(responseEdmEntitySet.getName());

		if(responseDataSource == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", responseEdmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}

		List<UriParameter> navigationKeyPredicates = uriResourceNavigation.getKeyPredicates();
		Map<String, UriParameter> navigationKeyPredicateMap = navigationKeyPredicates
				.stream()
				.collect(Collectors.toMap(UriParameter::getName, x -> x));
		
		Entity entity;
	    Object object = responseDataSource.readFromKey(navigationKeyPredicateMap, expandOption, selectOption, parentobject);

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
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
	    InputStream entityStream = serializerResult.getContent();

	    response.setContent(entityStream);
	    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}
	
	private void readFunctionImport(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceFunction uriResourceFunction = (UriResourceFunction) resourcePaths.get( uriInfo.getUriResourceParts().size() - 1 );
		org.apache.olingo.commons.api.edm.EdmFunction function = uriResourceFunction.getFunction();
		
		String operationName = function.getName();
		Operation<?> operation = operationsMap.get(operationName);
		
		Map<String, UriParameter> functionParameters = uriResourceFunction.getParameters()
			.stream()
			.collect(Collectors.toMap(UriParameter::getName, x -> x ));
		
		Class<?> clazz = operation.getClass();
		
		for (Field fld : getDeclaredFields(clazz)) {

			EdmParameter edmParameter = fld.getAnnotation(EdmParameter.class);
			if(edmParameter != null) {
				
				String parameterName = edmParameter.name().isEmpty() ? fld.getName() : edmParameter.name();
				if(edmParameter.name().trim().isEmpty()) {
					parameterName = Util.applyNamingConvention(edmParameter, parameterName);
				}
				UriParameter parameter = functionParameters.get(parameterName);
				
				if(fld.getType().isEnum()) {
					
					Class<?> fldClazz = fld.getType();
					EdmEnum edmEnum = fldClazz.getAnnotation(EdmEnum.class);
					
					if(edmEnum != null) {
						
						try {
							Enum<?>[] enums = (Enum<?>[]) fldClazz.getEnumConstants();
	    					Enum<?> rv = null;
														
	    					for(Enum<?> enumeration : enums) {
	    						if(parameter.getText().contains("'" + enumeration.name() + "'")) {
	    							rv = enumeration;
	    							break;
	    						}
	    					}
	    					
    						fld.setAccessible(true);
                    		fld.set(operation, rv);
	    					
						} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
							LOG.error(e.getMessage(), e);
							throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
						}
					}
					
				} else {
				
					String edmType = edmParameter.type().isEmpty() ? Util.inferEdmType(fld) : edmParameter.type();
					Object value = convertEdmType(edmType, parameter.getText());
					
					fld.setAccessible(true);
					try {
						fld.set(operation, value);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						LOG.error(e.getMessage(), e);
						throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
					}
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
			LOG.error(e.getMessage(), e);
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
			LOG.error(e.getMessage(), e);
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

		final String id = request.getRawBaseUri() + request.getRawODataPath();
		final Map<String, String> queryParams = new HashMap<>();

		if(request.getRawQueryPath() != null) {
			Stream.of(request.getRawQueryPath().split("&")).forEach(param -> {
				String[] pair = param.split("=");
				String key = pair[0];
				String value = pair[1];
				queryParams.put(key, value);
			});
		}
		
		if(!queryParams.containsKey("$count") && !queryParams.containsKey("%24count")) {
			queryParams.put("$count", "true");
		}
		queryParams.remove("$skip");
		queryParams.remove("%24skip");
		
		Iterable<?> data = dataSource.readAll(expandOption, filterOption, orderByOption, null);
		
		if(count) entityCollection.setCount(Iterables.size(data));
		
		if(skipOption != null) {
			data = Iterables.skip(data, skipOption.getValue());
			queryParams.put("$skip", String.valueOf(skipOption.getValue() + maxTopOption));
		} else {
			queryParams.put("$skip", String.valueOf(maxTopOption));
		}

		final List<String> pairs = new ArrayList<>();
		queryParams.entrySet().forEach(entry -> {
			pairs.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
		});
		String nextLink = pairs.stream().collect(Collectors.joining("&"));
		if(nextLink != null && !nextLink.isEmpty()) {
			nextLink = id + "?" + nextLink;
		}
		
		if(topOption != null) {
			if(Iterables.size(data) <= topOption.getValue()) {
				nextLink = null;
			}
			data = Iterables.limit(data, topOption.getValue());
		}
		
		try {			
			for(Object object : data) {
				Entity entity = writeEntity(object, expandOption);
				result.add(entity);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
				
		ODataSerializer serializer = odata.createSerializer(responseFormat);

	    ContextURL contextUrl = null;
		try {
			if(nextLink != null) {
				LOG.debug("NEXT LINK: {}", nextLink);
				entityCollection.setNext(new URI(nextLink));
			}
			
			contextUrl = ContextURL.with()
					.serviceRoot(new URI(SERVICE_ROOT))
					.entitySet(edmEntitySet)
					.selectList(selectList)
					.build();
		} catch (URISyntaxException e) {
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
		
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
	
	private Object readFromEntitySet(UriResourceEntitySet uriResourceEntitySet) throws ODataApplicationException {
		return readFromEntitySet(null, null, uriResourceEntitySet);
	}
	
	private Object readFromEntitySet(SelectOption selectOption, ExpandOption expandOption, UriResourceEntitySet uriResourceEntitySet) throws ODataApplicationException {

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
		
		Object object = dataSource.readFromKey(keyPredicateMap, expandOption, selectOption, null);
		
		return object;
	}
}
