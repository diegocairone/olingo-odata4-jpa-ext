package com.cairone.olingo.ext.jpa.processors;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.PrimitiveCollectionProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.springframework.context.ApplicationContext;

import com.cairone.olingo.ext.jpa.annotations.EdmEnum;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.cairone.olingo.ext.jpa.utilities.Util;

public class PrimitiveProcessorImpl extends BaseProcessor implements PrimitiveProcessor, PrimitiveCollectionProcessor {

	protected Map<String, DataSource> dataSourceMap = new HashMap<>();
	protected Map<String, Operation<?>> operationsMap = new HashMap<>();
	
	public PrimitiveProcessorImpl initialize(ApplicationContext context) throws ODataApplicationException {
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
				com.cairone.olingo.ext.jpa.annotations.EdmFunction edmFunction = operation.getClass().getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmFunction.class);
				if(edmFunction != null) {
					String operationName = edmFunction.name().isEmpty() ? operation.getClass().getSimpleName() : edmFunction.name();
					operationsMap.put(operationName, operation);
				}
			});
		
		return this;
	}

	@Override
	public PrimitiveProcessorImpl setServiceRoot(String ServiceRoot) {
		super.setServiceRoot(ServiceRoot);
		return this;
	}

	@Override
	public PrimitiveProcessorImpl setDefaultEdmPackage(String DefaultEdmPackage) {
		super.setDefaultEdmPackage(DefaultEdmPackage);
		return this;
	}

	// *** Primitive Processor Interface
	
	@Override
	public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		final UriResource lastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 1 );
		
		if(lastResourceSegment instanceof UriResourcePrimitiveProperty) {
			readPrimitiveAndPrimitiveCollectionProperty(request, response, uriInfo, responseFormat);
		} else if(lastResourceSegment instanceof UriResourceFunction) {
			readPrimitiveFunction(request, response, uriInfo, responseFormat);
		} else {
			throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}
	}
	
	@Override
	public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	// *** Primitive Collection Processor Interface
	
	@Override
	public void readPrimitiveCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		final UriResource lastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 1 );

		if(lastResourceSegment instanceof UriResourcePrimitiveProperty) {
			readPrimitiveAndPrimitiveCollectionProperty(request, response, uriInfo, responseFormat);
		} else if(lastResourceSegment instanceof UriResourceFunction) {
			readPrimitiveFunction(request, response, uriInfo, responseFormat);
		} else {
			throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}
	}

	@Override
	public void updatePrimitiveCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void deletePrimitiveCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	private void readPrimitiveAndPrimitiveCollectionProperty(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		// last segment is a primitive property
		final UriResource lastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 1 );
		UriResourceProperty uriProperty = (UriResourceProperty) lastResourceSegment;
		
		EdmProperty edmProperty = uriProperty.getProperty();
		EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) edmProperty.getType();
        
		String edmPropertyName = edmProperty.getName();
		
		// pre-last segment is an entity type
		final UriResource prelastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 2 );
		UriResourceEntitySet uriEntitySet = (UriResourceEntitySet) prelastResourceSegment;
		
		EdmEntitySet edmEntitySet = uriEntitySet.getEntitySet();

		Entity entity;
		Object object = readFromEntitySet(uriEntitySet);

		if(object == null) {
			throw new ODataApplicationException("Requested entity does not exist", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}

		try {	
			entity = writeEntity(object);
			
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		// retrieve the property data from the entity
        Property property = entity.getProperty(edmPropertyName);
        if (property == null) {
        	throw new ODataApplicationException("Property not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }
        
        // serialize
        Object value = property.getValue();
        if (value != null) {
        	
        	// configure the serializer
            ODataSerializer serializer = odata.createSerializer(responseFormat);

            ContextURL contextUrl = null;
            try {
            	contextUrl = ContextURL.with().serviceRoot(new URI(SERVICE_ROOT)).entitySet(edmEntitySet).navOrPropertyPath(edmPropertyName).build();
            } catch (URISyntaxException e) {
            	LOG.error(e.getMessage(), e);
    			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
    		}
            PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

            // serialize
            SerializerResult serializerResult;
            try {
	            if(property.isCollection()) {
	            	serializerResult = serializer.primitiveCollection(serviceMetadata, edmPropertyType, property, options);
	            } else {
	            	serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property, options);
	            }
	            
	            InputStream propertyStream = serializerResult.getContent();
	
	            // configure the response object
	            response.setContent(propertyStream);
	            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	            
    	    } catch (SerializerException e) {
    	    	LOG.error(e.getMessage(), e);
    			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
    		}
            
        } else {
        	// in case there's no value for the property, we can skip the serialization
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        }
	}
	
	private void readPrimitiveFunction(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		// last segment is a function with a primitive return value
		final UriResource lastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 1 );
		UriResourceFunction uriFunction = (UriResourceFunction) lastResourceSegment;
		
		EdmFunction edmFunction = uriFunction.getFunction();
		EdmReturnType edmReturnType = edmFunction.getReturnType();
		
		String operationName = edmFunction.getName();
		Operation<?> operation = operationsMap.get(operationName);
		
		Map<String, UriParameter> functionParameters = uriFunction.getParameters()
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

		Map<String, UriParameter> keyPredicateMap = null;
		
		if(edmFunction.isBound()) {
			final UriResource prelastResourceSegment = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() - 2 );
			UriResourceEntitySet uriEntitySet = (UriResourceEntitySet) prelastResourceSegment;
			List<UriParameter> keyPredicates = uriEntitySet.getKeyPredicates();
			keyPredicateMap = keyPredicates.stream().collect(Collectors.toMap(UriParameter::getName, x -> x));
		}
		
		Object returnedValue;
		
		try {
			returnedValue = operation.doOperation(edmFunction.isBound(), keyPredicateMap);
			
		} catch (ODataException | IllegalArgumentException | SecurityException e) {
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		EdmPrimitiveType edmFunctionReturnType = (EdmPrimitiveType) edmReturnType.getType();
		Property property = new Property(edmFunctionReturnType.getName(), null,
				edmReturnType.isCollection() ? ValueType.COLLECTION_PRIMITIVE : ValueType.PRIMITIVE, returnedValue);
		
		// configure the serializer
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        
        ContextURL contextUrl = null;
        try {
        	contextUrl = ContextURL
        		.with()
        		.serviceRoot(new URI(SERVICE_ROOT))
        		.entitySetOrSingletonOrType(edmFunctionReturnType.getFullQualifiedName().getFullQualifiedNameAsString())
        		.build();
        } catch (URISyntaxException e) {
        	LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		}
        PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

        // serialize
        SerializerResult serializerResult;
        try {
	        
	        if(property.isCollection()) {
	        	serializerResult = serializer.primitiveCollection(serviceMetadata, edmFunctionReturnType, property, options);
	        } else {
	        	serializerResult = serializer.primitive(serviceMetadata, edmFunctionReturnType, property, options);
	        }
	
	        InputStream propertyStream = serializerResult.getContent();
	
	        // configure the response object
	        response.setContent(propertyStream);
	        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	        
	    } catch (SerializerException e) {
	    	LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
	}

	private Object readFromEntitySet(UriResourceEntitySet uriResourceEntitySet) throws ODataApplicationException {

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
		
		Object object = dataSource.readFromKey(keyPredicateMap, null, null, null);
		
		return object;
	}
}
