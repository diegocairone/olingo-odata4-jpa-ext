package com.cairone.olingo.ext.demo.ctrls;

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
import com.cairone.olingo.ext.jpa.processors.ComplexProcessorImpl;
import com.cairone.olingo.ext.jpa.processors.MediaProcessor;
import com.cairone.olingo.ext.jpa.processors.PrimitiveProcessorImpl;
import com.cairone.olingo.ext.jpa.providers.EdmProvider;

@Component
public class ODataController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Autowired private EdmProvider demoEdmProvider = null;
	@Autowired private ActionProcessor actionProcessor = null;
	@Autowired private BatchRequestProcessor batchRequestProcessor = null;
	@Autowired private MediaProcessor mediaProcessor = null;
	@Autowired private PrimitiveProcessorImpl primitiveProcessor = null;
	@Autowired private ComplexProcessorImpl complexProcessor = null;
	
	public void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException {
		
		try {
			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(demoEdmProvider, new ArrayList<EdmxReference>());
			
			ODataHttpHandler handler = odata.createHandler(edm);

			handler.register(actionProcessor);
			handler.register(batchRequestProcessor);
			handler.register(mediaProcessor);
			handler.register(primitiveProcessor);
			handler.register(complexProcessor);
			handler.register(new DefaultDebugSupport());
			
			handler.process(servletRequest, servletResponse);
			
		} catch (RuntimeException e) {
			throw new ServletException(e);
		}
	}
}

