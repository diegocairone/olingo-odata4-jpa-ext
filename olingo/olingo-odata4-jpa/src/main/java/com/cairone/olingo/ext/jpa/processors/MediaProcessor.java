package com.cairone.olingo.ext.jpa.processors;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.springframework.context.ApplicationContext;

import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.MediaDataSource;

public class MediaProcessor extends EntitySetProcessor implements MediaEntityProcessor {

	@Override
	public MediaProcessor initialize(ApplicationContext context) throws ODataApplicationException {
		super.initialize(context);
		return this;
	}

	@Override
	public MediaProcessor setServiceRoot(String ServiceRoot) {
		super.setServiceRoot(ServiceRoot);
		return this;
	}

	@Override
	public MediaProcessor setDefaultEdmPackage(String DefaultEdmPackage) {
		super.setDefaultEdmPackage(DefaultEdmPackage);
		return this;
	}

	@Override
	public MediaProcessor setMaxTopOption(Integer maxTopOption) {
		super.setMaxTopOption(maxTopOption);
		return this;
	}

	@Override
	public void readMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		final UriResource firstResoucePart = uriInfo.getUriResourceParts().get(0);
		
	    if(firstResoucePart instanceof UriResourceEntitySet) {
	    	
	    	final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) firstResoucePart;
	    	final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    	
	    	DataSource dataSource = dataSourceMap.get(edmEntitySet.getName());
	    	
	    	List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		    Map<String, UriParameter> keyPredicateMap = keyPredicates
					.stream()
					.collect(Collectors.toMap(UriParameter::getName, x -> x));
			
		    MediaDataSource mediaDataSource = (MediaDataSource) dataSource;
		    byte[] binary = mediaDataSource.findMediaResource(keyPredicateMap);
			
		    try {
				
				final InputStream responseContent = odata.createFixedFormatSerializer().binary(binary);
				final String contentType = URLConnection.guessContentTypeFromStream(responseContent);
				
				response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		        response.setContent(responseContent);
		        response.setHeader(HttpHeader.CONTENT_TYPE, contentType);
				
		        return;
		        
			} catch (IOException e) {
				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
	    }
	    
	    throw new ODataApplicationException("Not implemented", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void createMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		final UriResource firstResoucePart = uriInfo.getUriResourceParts().get(0);
		
	    if(firstResoucePart instanceof UriResourceEntitySet) {
	    	
	    	final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) firstResoucePart;
	    	final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    	
	    	DataSource dataSource = dataSourceMap.get(edmEntitySet.getName());
	    	
		    MediaDataSource mediaDataSource = (MediaDataSource) dataSource;
		    Entity entity;
		    
		    try {
		    	byte[] binary = odata.createFixedFormatDeserializer().binary(request.getBody());
		    	Object object = mediaDataSource.createMediaResource(binary);
				
				try {
					entity = writeEntity(object, null);
					
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
					throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
		        
			} catch (ODataApplicationException e) {
				throw e;
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
		    final EntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextUrl).build();
		    final SerializerResult serializerResult = odata
		    		.createSerializer(responseFormat)
		    		.entity(serviceMetadata, edmEntitySet.getEntityType(), entity, opts);

		    final String location = request.getRawBaseUri() + '/' + odata.createUriHelper().buildCanonicalURL(edmEntitySet, entity);

		    response.setContent(serializerResult.getContent());
		    response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		    response.setHeader(HttpHeader.LOCATION, location);
		    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
		    
		    return;
	    }
	    
	    throw new ODataApplicationException("Not implemented", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void updateMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		final UriResource firstResoucePart = uriInfo.getUriResourceParts().get(0);
		
	    if(firstResoucePart instanceof UriResourceEntitySet) {
	    	
	    	final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) firstResoucePart;
	    	final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    	
	    	DataSource dataSource = dataSourceMap.get(edmEntitySet.getName());
	    	
	    	List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		    Map<String, UriParameter> keyPredicateMap = keyPredicates
					.stream()
					.collect(Collectors.toMap(UriParameter::getName, x -> x));
			
		    MediaDataSource mediaDataSource = (MediaDataSource) dataSource;
		    
		    try {
		    	byte[] binary = odata.createFixedFormatDeserializer().binary(request.getBody());
				mediaDataSource.updateMediaResource(keyPredicateMap, binary);

				final InputStream responseContent = odata.createFixedFormatSerializer().binary(binary);
				final String contentType = URLConnection.guessContentTypeFromStream(responseContent);
				
				response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		        response.setContent(responseContent);
		        response.setHeader(HttpHeader.CONTENT_TYPE, contentType);
				
		        return;
		        
			} catch (ODataApplicationException e) {
				throw e;
			} catch (IOException e) {
				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
	    }
	    
	    throw new ODataApplicationException("Not implemented", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public void deleteMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
		deleteEntity(request, response, uriInfo);
	}
}
