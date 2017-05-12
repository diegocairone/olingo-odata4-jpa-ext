package com.cairone.olingo.ext.jpa.processors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.springframework.context.ApplicationContext;

import com.cairone.olingo.ext.jpa.interfaces.DataSourceProvider;
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
	public void readMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		
		final UriResource firstResoucePart = uriInfo.getUriResourceParts().get(0);
		
	    if(firstResoucePart instanceof UriResourceEntitySet) {
	    	
	    	final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) firstResoucePart;
	    	final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    	
	    	DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
	    	
	    	List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		    Map<String, UriParameter> keyPredicateMap = keyPredicates
					.stream()
					.collect(Collectors.toMap(UriParameter::getName, x -> x));
			
		    MediaDataSource mediaDataSource = (MediaDataSource) dataSourceProvider;
		    
		    try {
				byte[] binary = mediaDataSource.findMediaResource(keyPredicateMap);
				
				final InputStream responseContent = odata.createFixedFormatSerializer().binary(binary);
				final String contentType = URLConnection.guessContentTypeFromStream(responseContent);
				
				response.setStatusCode(HttpStatusCode.OK.getStatusCode());
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
	public void createMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		//FIXME
		/*
		if(requestFormat.equals(ContentType.APPLICATION_JSON)) {
			createEntity(request, response, uriInfo, requestFormat, responseFormat);
			return;
		}
		*/
		createEntity(request, response, uriInfo, requestFormat, responseFormat);
	}

	@Override
	public void updateMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		//createMediaEntity(request, response, uriInfo, requestFormat, responseFormat);

		final UriResource firstResoucePart = uriInfo.getUriResourceParts().get(0);
		
	    if(firstResoucePart instanceof UriResourceEntitySet) {
	    	
	    	final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) firstResoucePart;
	    	final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    	
	    	DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
	    	
	    	List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		    Map<String, UriParameter> keyPredicateMap = keyPredicates
					.stream()
					.collect(Collectors.toMap(UriParameter::getName, x -> x));
			
		    MediaDataSource mediaDataSource = (MediaDataSource) dataSourceProvider;
		    
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
		/*
		final UriResource firstResoucePart = uriInfo.getUriResourceParts().get(0);
		
	    if(firstResoucePart instanceof UriResourceEntitySet) {
	    	
	    	final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) firstResoucePart;
	    	final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    	
	    	DataSourceProvider dataSourceProvider = dataSourceProviderMap.get(edmEntitySet.getName());
	    	
	    	List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		    Map<String, UriParameter> keyPredicateMap = keyPredicates
					.stream()
					.collect(Collectors.toMap(UriParameter::getName, x -> x));
			
		    MediaDataSource mediaDataSource = (MediaDataSource) dataSourceProvider;
		    
		    try {
				mediaDataSource.deleteMediaResource(keyPredicateMap);
				response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
				
		        return;
		        
			} catch (ODataApplicationException e) {
				throw e;
			}
	    }
	    
	    throw new ODataApplicationException("Not implemented", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);*/
	}
}
