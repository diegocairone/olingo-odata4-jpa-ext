package com.cairone.odataexample.ctrls;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.debug.DefaultDebugSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.olingo.ext.jpa.processors.ActionProcessor;
import com.cairone.olingo.ext.jpa.processors.BatchRequestProcessor;
import com.cairone.olingo.ext.jpa.processors.MediaProcessor;
import com.cairone.olingo.ext.jpa.processors.PrimitiveProcessorImpl;
import com.cairone.olingo.ext.jpa.providers.OdataexampleEdmProvider;

@Component 
public class ODataController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Autowired private OdataexampleEdmProvider odataexampleEdmProvider = null;
	@Autowired private ActionProcessor actionProcessor = null;
	@Autowired private BatchRequestProcessor batchRequestProcessor = null;
	
	//@Autowired @Qualifier("getEntitySetProcessor") private EntitySetProcessor entitySetProcessor = null;
	@Autowired private MediaProcessor mediaProcessor = null;
	@Autowired private PrimitiveProcessorImpl primitiveProcessorImpl = null;
	
	public void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException {
		
		try {
			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(odataexampleEdmProvider, new ArrayList<EdmxReference>());
			
			ODataHttpHandler handler = odata.createHandler(edm);
			
			//handler.register(entitySetProcessor);
			handler.register(actionProcessor);
			handler.register(batchRequestProcessor);
			handler.register(mediaProcessor);
			handler.register(primitiveProcessorImpl);
			handler.register(new DefaultDebugSupport());
			
			handler.process(servletRequest, servletResponse);
			
		} catch (RuntimeException e) {
			//LOG.error("Server Error occurred in ExampleServlet", e);
			throw new ServletException(e);
		}
	}
}
