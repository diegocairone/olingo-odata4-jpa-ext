package com.cairone.olingo.ext.demo.cfg;

import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.ctrls.ODataController;
import com.cairone.olingo.ext.jpa.processors.ActionProcessor;
import com.cairone.olingo.ext.jpa.processors.BatchRequestProcessor;
import com.cairone.olingo.ext.jpa.processors.ComplexProcessorImpl;
import com.cairone.olingo.ext.jpa.processors.MediaProcessor;
import com.cairone.olingo.ext.jpa.processors.PrimitiveProcessorImpl;
import com.cairone.olingo.ext.jpa.providers.EdmProvider;

@Configuration
public class OlingoConfig {
	
	@Value("${demo.odata.maxtopoption}") private Integer maxTopOption = null;
	@Value("${demo.odata.serviceroot}") public String SERVICE_ROOT = null;
	
    @Autowired private ApplicationContext context = null;
    @Autowired ODataController dispatcherServlet = null;
    
    @Bean
    public PrimitiveProcessorImpl getPrimitiveProcessor() throws ODataApplicationException {
    	
    	PrimitiveProcessorImpl primitiveProcessor = new PrimitiveProcessorImpl()
	    	.setDefaultEdmPackage(AppDemoConstants.DEFAULT_EDM_PACKAGE)
			.setServiceRoot(SERVICE_ROOT)
			.initialize(context);
    	
    	return primitiveProcessor;
    }

    @Bean
    public ComplexProcessorImpl getComplexProcessor() throws ODataApplicationException {
    	
    	ComplexProcessorImpl complexProcessor = new ComplexProcessorImpl()
	    	.setDefaultEdmPackage(AppDemoConstants.DEFAULT_EDM_PACKAGE)
			.setServiceRoot(SERVICE_ROOT)
			.initialize(context);
    	
    	return complexProcessor;
    }

    @Bean
    public MediaProcessor getMediaProcessor() throws ODataApplicationException {
    	
    	MediaProcessor mediaProcessor = new MediaProcessor()
	    	.setDefaultEdmPackage(AppDemoConstants.DEFAULT_EDM_PACKAGE)
			.setServiceRoot(SERVICE_ROOT)
			.setMaxTopOption(maxTopOption)
			.initialize(context);
    	
    	return mediaProcessor;
    }
    
    @Bean
    public ActionProcessor getActionProcessor() throws ODataApplicationException {

    	ActionProcessor processor = new ActionProcessor()
    		.setDefaultEdmPackage(AppDemoConstants.DEFAULT_EDM_PACKAGE)
    		.setServiceRoot(SERVICE_ROOT)
    		.initialize(context);
    	
    	return processor;
    }
    
    @Bean
    public BatchRequestProcessor getBatchRequestProcessor() {
    	BatchRequestProcessor processor = new BatchRequestProcessor();
    	return processor;
    }
    
    @Bean
    public EdmProvider getOdataexampleEdmProvider() throws ODataApplicationException {

    	EdmProvider provider = new EdmProvider()
    		.setContainerName(AppDemoConstants.CONTAINER_NAME)
    		.setDefaultEdmPackage(AppDemoConstants.DEFAULT_EDM_PACKAGE)
    		.setNameSpace(AppDemoConstants.NAME_SPACE)
    		.setServiceRoot(SERVICE_ROOT)
    		.initialize();
    	
    	return provider;
    }

    @Bean
    public ServletRegistrationBean dispatcherServletRegistration() {
    	ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet, "/odata/olingodemo.svc/*");
    	return registration;
    }
}
