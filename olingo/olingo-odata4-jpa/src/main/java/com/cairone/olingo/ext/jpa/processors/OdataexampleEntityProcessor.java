package com.cairone.olingo.ext.jpa.processors;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
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
import java.util.Set;
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
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.DataSourceProvider;
import com.google.common.collect.Iterables;

public class OdataexampleEntityProcessor implements EntityProcessor, EntityCollectionProcessor {

	private String SERVICE_ROOT = null;
	private String DEFAULT_EDM_PACKAGE = null;
	
	private OData odata;
	private ServiceMetadata serviceMetadata;
	
	private Map<String, DataSourceProvider> dataSourceProviderMap = new HashMap<>();
	private Map<String, Class<?>> entitySetMap = new HashMap<>();
	
	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}
	
	public OdataexampleEntityProcessor initialize(ApplicationContext context) throws ODataApplicationException {
		
		context.getBeansOfType(DataSourceProvider.class).entrySet()
			.stream()
			.forEach(entry -> {
				DataSourceProvider dataSourceProvider = entry.getValue();
				dataSourceProviderMap.put(dataSourceProvider.isSuitableFor(), dataSourceProvider);
			});

		ClassPathScanningCandidateComponentProvider provider = createComponentScanner(Arrays.asList(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class));
		Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(DEFAULT_EDM_PACKAGE);

		try {
			for(BeanDefinition beanDef : beanDefinitions) {
				Class<?> cl = Class.forName(beanDef.getBeanClassName());
				
				com.cairone.olingo.ext.jpa.annotations.EdmEntitySet edmEntitySet = cl.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class);
				
				if(edmEntitySet != null) {
					entitySetMap.put(edmEntitySet.value(), cl);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		return this;
	}

	@Override
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		
		DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
		
		if(dataSourceProvider == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		DataSource dataSource = dataSourceProvider.getDataSource();
		
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();
		
		Class<?> clazz = entitySetMap.get(edmEntitySet.getName());
		Object object = null, createdObject;
		
    	EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
    	String[] keys = edmEntity.key();
    	Map<String, Object> keyValues = Arrays.asList(keys)
    		.stream()
    		.collect(Collectors.toMap(x -> x, x -> x));
    	
    	try {
    		
			Constructor<?> constructor = clazz.getConstructor();
			object = constructor.newInstance();
			
			writeObject(clazz, object, requestEntity);
    		
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
    	
		Entity createdEntity = new Entity();
		
		try
		{
	    	createdObject = dataSource.create(object);
	    	for(Field fld : createdObject.getClass().getDeclaredFields()) {
	    		
	    		com.cairone.olingo.ext.jpa.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
				
	            if (edmProperty != null) {
	            	
	            	fld.setAccessible(true);
	            	
	            	String name = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
	            	Object value = fld.get(createdObject);
	            	
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
	            	
	            	if(keyValues.containsKey(name)) {
	            		keyValues.put(name, value);
	            	}
	            }
	    	}
		} catch(ODataException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
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

		DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
		
		if(dataSourceProvider == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		DataSource dataSource = dataSourceProvider.getDataSource();
		
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

    	try {
	    	
			Constructor<?> constructor = clazz.getConstructor();
			object = constructor.newInstance();

			writeObject(clazz, object, requestEntity);
    		
    	} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
    	
		try {
			dataSource.update(keyPredicateMap, object, propertiesInJSON, request.getMethod().equals(HttpMethod.PUT));
		} catch(ODataException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		  
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		
		DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
		
		if(dataSourceProvider == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		DataSource dataSource = dataSourceProvider.getDataSource();
		
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		Map<String, UriParameter> keyPredicateMap = keyPredicates
				.stream()
				.collect(Collectors.toMap(UriParameter::getName, x -> x));
		
    	try {
    		dataSource.delete(keyPredicateMap);
		} catch (Exception e) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
    	
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
	    String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption);
	    
		DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
		
		if(dataSourceProvider == null) {
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
	    
		try {
			Object object = dataSourceProvider.readFromKey(keyPredicateMap);
			
			if(object == null) {
				throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
			}
			
			entity = writeEntity(object, uriInfo.getExpandOption());
			
		} catch (ODataException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		EdmEntityType entityType = edmEntitySet.getEntityType();
		
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
	    SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entity, options);
	    InputStream entityStream = serializerResult.getContent();

	    response.setContent(entityStream);
	    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@Override
	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
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
	    
	    String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption);
	    boolean count = countOption == null ? false : countOption.getValue();
	    
		DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
		
		if(dataSourceProvider == null) {
			throw new ODataApplicationException(
					String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", edmEntitySet.getName()), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH);
		}
		
		EntityCollection entityCollection = new EntityCollection();
		List<Entity> result = entityCollection.getEntities();
		
		try {
			Iterable<?> data = dataSourceProvider.readAll(expandOption, filterOption, orderByOption);
			
			if(count) entityCollection.setCount(Iterables.size(data));
			
			if(skipOption != null) {
				data = Iterables.skip(data, skipOption.getValue());
			}
			
			if(topOption != null) {
				data = Iterables.limit(data, topOption.getValue()); 
			}
			
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

	public String getServiceRoot() {
		return SERVICE_ROOT;
	}

	public OdataexampleEntityProcessor setServiceRoot(String ServiceRoot) {
		SERVICE_ROOT = ServiceRoot;
		return this;
	}

	public String getDefaultEdmPackage() {
		return DEFAULT_EDM_PACKAGE;
	}

	public OdataexampleEntityProcessor setDefaultEdmPackage(String DefaultEdmPackage) {
		DEFAULT_EDM_PACKAGE = DefaultEdmPackage;
		return this;
	}

	private ClassPathScanningCandidateComponentProvider createComponentScanner(Iterable<Class<? extends Annotation>> annotationTypes) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		for(Class<? extends Annotation> annotationType : annotationTypes) provider.addIncludeFilter(new AnnotationTypeFilter(annotationType));
		return provider;
    }
	
	private Entity writeEntity(Object object, ExpandOption expandOption) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, ODataApplicationException {
		
		Entity entity = new Entity();
		Class<?> clazz = object.getClass();
		
		com.cairone.olingo.ext.jpa.annotations.EdmEntitySet edmEntitySet = clazz.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class);
		com.cairone.olingo.ext.jpa.annotations.EdmEntity edmEntity = clazz.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntity.class);
		
		String edmEntitySetName = edmEntitySet.value().isEmpty() ? clazz.getSimpleName() : edmEntitySet.value();
		
		String[] keys = edmEntity.key();
    	Map<String, Object> keyValues = Arrays.asList(keys)
    		.stream()
    		.collect(Collectors.toMap(x -> x, x -> x));
    	
    	Map<String, EdmNavigationProperty> edmNavigationPropertyMap = new HashMap<String, EdmNavigationProperty>();
    	if(expandOption != null && !expandOption.getExpandItems().isEmpty()) {
    		expandOption.getExpandItems().forEach(expandItem -> {
    			UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
    			if(uriResource instanceof UriResourceNavigation) {
    				EdmNavigationProperty edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
    				String navPropName = edmNavigationProperty.getName();
    				edmNavigationPropertyMap.put(navPropName, edmNavigationProperty);
    			}
    		});
    	}
		
		for(Field fld : object.getClass().getDeclaredFields()) {

    		com.cairone.olingo.ext.jpa.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
			
            if (edmProperty != null) {
            	
            	fld.setAccessible(true);
            	
            	String name = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
            	Object value = fld.get(object);
            	
            	if(value != null) {
            		
	            	if(value instanceof LocalDate) {
	            		
	            		LocalDate localDateValue = (LocalDate) value;
	            		entity.addProperty(new Property(null, name, ValueType.PRIMITIVE, GregorianCalendar.from(localDateValue.atStartOfDay(ZoneId.systemDefault()))));
	            	
	            	} else if(value.getClass().isEnum()) {
	            		
	            		Class<?> fldClazz = fld.getType();
	            		Method getValor = fldClazz.getMethod("getValor");
						Enum<?>[] enums = (Enum<?>[]) fldClazz.getEnumConstants();
						
						Object rvValue = getValor.invoke(value);
						
						for(Enum<?> enumeration : enums) {
							Object rv = getValor.invoke(enumeration);
							if(rvValue.equals(rv)) {
	    						entity.addProperty(new Property(null, name, ValueType.ENUM, rv));
	                    		break;
							}
						}
	            	} else {
	            		entity.addProperty(new Property(null, name, ValueType.PRIMITIVE, value));
	            	}
	            	
	            	if(keyValues.containsKey(name)) {
	            		keyValues.put(name, value);
	            	}
            	}
            }
            
            com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty edmNavigationProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty.class);
			
            if(edmNavigationProperty != null) {
            	
            	String navigationPropertyName = edmNavigationProperty.name().isEmpty() ? fld.getName() : edmNavigationProperty.name();
            	EdmNavigationProperty navigationProperty = edmNavigationPropertyMap.get(navigationPropertyName);
            	
            	if(navigationProperty != null) {

            		fld.setAccessible(true);
            		
            		Class<?> fieldClass = fld.getType();
            		Object inlineEntity = fld.get(object);

            		Link link = new Link();
					link.setTitle(navigationPropertyName);
					
    				if(Collection.class.isAssignableFrom(fieldClass)) {
    					
    					EntityCollection data = new EntityCollection();
    					
    					@SuppressWarnings("unchecked")
						Collection<Object> objects = (Collection<Object>) inlineEntity;
    					
    					for(Object item : objects) {
    						Entity expandEntity = writeEntity(item, null);
    						data.getEntities().add(expandEntity);
    					}
    					
    					link.setInlineEntitySet(data);
    					
    				} else {
    					Entity expandEntity = writeEntity(inlineEntity, null);
    					link.setInlineEntity(expandEntity);
    				}

					entity.getNavigationLinks().add(link);
            	}
            }
		}
		
		String entityID = keyValues.entrySet().stream().map(Entry::toString).collect(Collectors.joining(",", "(", ")"));
		try {
			entity.setId(new URI(edmEntitySetName + entityID));
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
				
		return entity;
	}
	
	private void writeObject(Class<?> clazz, Object object, Entity entity) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		if(object == null) {
			
			Constructor<?> constructor = clazz.getConstructor();
			object = constructor.newInstance();
		}
		
		for (Field fld : clazz.getDeclaredFields()) {
			
			com.cairone.olingo.ext.jpa.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
			
			if (edmProperty != null) {
            	
            	String propertyName = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
            	Property property = entity.getProperty(propertyName);
            	
            	if(property != null) {
            		
    				Class<?> fldClazz = fld.getType();
    				com.cairone.olingo.ext.jpa.annotations.EdmEnum edmEnum = fldClazz.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEnum.class);
    				
    				if(edmEnum != null) {
    					
    					Method setValor = fldClazz.getMethod("setValor", Integer.TYPE);
    					Enum<?>[] enums = (Enum<?>[]) fldClazz.getEnumConstants();
    					
    					for(Enum<?> enumeration : enums) {
    						Object rv = setValor.invoke(enumeration, property.asEnum());
    						fld.setAccessible(true);
                    		fld.set(object, rv);
                    		break;
    					}
    					
    				} else {
	    				
	            		if(fld.getType().isAssignableFrom(LocalDate.class) && property.getValue() instanceof GregorianCalendar) {
	
	                		GregorianCalendar cal = (GregorianCalendar) property.getValue();
	                		
	                		fld.setAccessible(true);
	                		fld.set(object, cal.toZonedDateTime().toLocalDate());
	                		
	            		} else {
	
	                		fld.setAccessible(true);
	                		fld.set(object, property.getValue());
	            		}
    				}
            	}
            }
			
			com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty edmNavigationProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty.class);
			
        	if(edmNavigationProperty != null) {
        		
        		String propertyName = edmNavigationProperty.name().isEmpty() ? fld.getName() : edmNavigationProperty.name();

        		Class<?> fieldClass = fld.getType();

				if(Collection.class.isAssignableFrom(fieldClass)) {
					//FIXME
				} else {

					fld.setAccessible(true);
					Object navpropField = fld.get(object);

					com.cairone.olingo.ext.jpa.annotations.EdmEntitySet targetEdmEntitySet = fieldClass.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class);
					String targetEntitySetName = targetEdmEntitySet.value();
					Class<?> cl = entitySetMap.get(targetEntitySetName);

					if(navpropField == null) {
						Constructor<?> c = cl.getConstructor();
    					navpropField = c.newInstance();
    					fld.set(object, navpropField);
					}
					
					Link link = entity.getNavigationLink(propertyName);
					if(link != null) {
						writeObject(cl, navpropField, link.getInlineEntity());
					}
				}
        	}
		}		
	}
}
