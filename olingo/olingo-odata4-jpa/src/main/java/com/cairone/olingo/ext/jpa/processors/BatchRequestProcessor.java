package com.cairone.olingo.ext.jpa.processors;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.apache.olingo.server.api.deserializer.batch.BatchOptions;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.apache.olingo.server.api.processor.BatchProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class BatchRequestProcessor extends BaseProcessor implements BatchProcessor {
	
	@Autowired TransactionTemplate transactionTemplate = null;
	
	@Override
	public void processBatch(BatchFacade facade, ODataRequest request, ODataResponse response) throws ODataApplicationException, ODataLibraryException {
		
		final String boundary = facade.extractBoundaryFromContentType(request.getHeader(HttpHeader.CONTENT_TYPE));
		
		final BatchOptions options = BatchOptions.with().rawBaseUri(request.getRawBaseUri())
                .rawServiceResolutionUri(request.getRawServiceResolutionUri())
                .build();
		
		final List<BatchRequestPart> requestParts = odata.createFixedFormatDeserializer().parseBatchRequest(request.getBody(), boundary, options);
		final List<ODataResponsePart> responseParts = new ArrayList<ODataResponsePart>();
		
	    for(final BatchRequestPart part : requestParts) {
	    	responseParts.add(facade.handleBatchRequest(part));
	    }

	    final String responseBoundary = "batch_" + UUID.randomUUID().toString();
	    final InputStream responseContent = odata.createFixedFormatSerializer().batchResponse(responseParts, responseBoundary);

	    response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.MULTIPART_MIXED + ";boundary=" + responseBoundary);
	    response.setContent(responseContent);
	    response.setStatusCode(HttpStatusCode.ACCEPTED.getStatusCode());
	}

	@Override
	public ODataResponsePart processChangeSet(BatchFacade facade, List<ODataRequest> requests) throws ODataApplicationException, ODataLibraryException {
		
		final List<ODataResponse> responses = new ArrayList<ODataResponse>();
		
		return transactionTemplate.execute(new TransactionCallback<ODataResponsePart>() {

			@Override
			public ODataResponsePart doInTransaction(TransactionStatus status) {
				
				try
				{
					for(final ODataRequest request : requests) {
						
						final ODataResponse response = facade.handleODataRequest(request);
						final int statusCode = response.getStatusCode();
						
						if(statusCode < 400) {
							responses.add(response);
						} else {
							status.setRollbackOnly();
							return new ODataResponsePart(response, false);
						}
					}
				} catch(ODataApplicationException | ODataLibraryException e) {
					status.setRollbackOnly();
			    }
				
				return new ODataResponsePart(responses, true);
			}
		});
	}
}
