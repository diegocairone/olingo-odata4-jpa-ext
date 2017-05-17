package com.cairone.olingo.ext.jpa.processors;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmStream;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.springframework.context.ApplicationContext;

import com.cairone.olingo.ext.jpa.interfaces.DataSourceProvider;

public class PrimitiveProcessorImpl extends BaseProcessor implements PrimitiveProcessor {

	protected Map<String, DataSourceProvider> dataSourceProviderMap = new HashMap<>();
	
	@Override
	public PrimitiveProcessorImpl initialize(ApplicationContext context) throws ODataApplicationException {
		super.initialize(context);

		context.getBeansOfType(DataSourceProvider.class).entrySet()
			.stream()
			.forEach(entry -> {
				DataSourceProvider dataSourceProvider = entry.getValue();
				dataSourceProviderMap.put(dataSourceProvider.isSuitableFor(), dataSourceProvider);
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

	@Override
	public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		final UriResource firstResoucePart = uriInfo.getUriResourceParts().get(0);
		final UriResource lastResoucePart = uriInfo.getUriResourceParts().get( uriInfo.getUriResourceParts().size() -  1 );
		
	    if(firstResoucePart instanceof UriResourceEntitySet) {

	    	final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) firstResoucePart;
	    	final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

	    	UriResourceProperty uriProperty = (UriResourceProperty) lastResoucePart;
	    	EdmProperty edmProperty = uriProperty.getProperty();
	    	String edmPropertyName = edmProperty.getName();
	    	EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) edmProperty.getType();
	    	
	    	DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
	    	
	    	List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		    Map<String, UriParameter> keyPredicateMap = keyPredicates
					.stream()
					.collect(Collectors.toMap(UriParameter::getName, x -> x));
			
		    try {
				Object object = dataSourceProvider.readFromKey(keyPredicateMap);
				Field field = null;
				
				for(Field fld : object.getClass().getDeclaredFields()) {
					com.cairone.olingo.ext.jpa.annotations.EdmProperty annotatedEdmProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
					if (annotatedEdmProperty != null) {
						String name = annotatedEdmProperty.name().isEmpty() ? fld.getName() : annotatedEdmProperty.name();
						if(name.equals(edmPropertyName)) {
							field = fld;
							break;
						}
					}
				}
				
				field.setAccessible(true);
				Object fieldValue = field.get(object);
				
				if(fieldValue == null) {
					response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
					return;
				}
				
				if(edmPropertyType instanceof EdmStream && fieldValue instanceof byte[]) {
					
					final InputStream responseContent = odata.createFixedFormatSerializer().binary((byte[])fieldValue);
					final String contentType = URLConnection.guessContentTypeFromStream(responseContent);
					
					response.setContent(responseContent);
					response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			        response.setHeader(HttpHeader.CONTENT_TYPE, contentType);
					
			        return;
			        
		    	} else {
		    		
		    		Entity entity = writeEntity(object, null);
		    		Property property = entity.getProperty(edmPropertyName);
		            
		    		if(property == null) {
		                 throw new ODataApplicationException("Property not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		            }
		    		
		    		ODataSerializer serializer = odata.createSerializer(responseFormat);

		    		ContextURL contextUrl = null;
					try {
						contextUrl = ContextURL.with()
								.serviceRoot(new URI(SERVICE_ROOT))
								.entitySet(edmEntitySet)
								.navOrPropertyPath(edmPropertyName)
								.build();
					} catch (URISyntaxException e) {
						throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
					}
		    		PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
		              
		    		SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property, options);
		    		InputStream propertyStream = serializerResult.getContent();

		    		response.setContent(propertyStream);
		    		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		    		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
		    		
		    		return;
		    	}
				
			} catch (ODataException | IllegalArgumentException | IllegalAccessException | IOException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
	    }

	    throw new ODataApplicationException("Not implemented", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("Not implemented", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
		throw new ODataApplicationException("Not implemented", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}
}
