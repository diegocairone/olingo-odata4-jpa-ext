package com.cairone.odataexample;

import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.cairone.odataexample.ctrls.ODataController;
import com.cairone.olingo.ext.jpa.processors.ActionProcessor;
import com.cairone.olingo.ext.jpa.processors.BatchRequestProcessor;
import com.cairone.olingo.ext.jpa.processors.EntitySetProcessor;
import com.cairone.olingo.ext.jpa.providers.OdataexampleEdmProvider;

@SpringBootApplication
public class OdataExample extends SpringBootServletInitializer
{
	public static final String NAME_SPACE = "com.cairone.odataexample";
	public static final String CONTAINER_NAME = "ODataExample";
	public static final String SERVICE_ROOT = "http://localhost:8080/odata/appexample.svc/";
	public static final String DEFAULT_EDM_PACKAGE = "com.cairone.odataexample.edm.resources";
	
    public static void main( String[] args ) {
        SpringApplication.run(OdataExample.class, args);
    }
  
    @Autowired private ApplicationContext context = null;
    @Autowired ODataController dispatcherServlet = null;
    
    @Bean
    public ActionProcessor getActionProcessor() throws ODataApplicationException {
    	
    	ActionProcessor processor = new ActionProcessor()
    		.setDefaultEdmPackage(DEFAULT_EDM_PACKAGE)
    		.setServiceRoot(SERVICE_ROOT)
    		.initialize(context);
    	
    	return processor;
    }
    
    @Bean
    public EntitySetProcessor getEntitySetProcessor() throws ODataApplicationException {
    	
    	EntitySetProcessor processor = new EntitySetProcessor()
    		.setDefaultEdmPackage(DEFAULT_EDM_PACKAGE)
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
    public OdataexampleEdmProvider getOdataexampleEdmProvider() throws ODataApplicationException {
    	
    	OdataexampleEdmProvider provider = new OdataexampleEdmProvider()
    		.setContainerName(CONTAINER_NAME)
    		.setDefaultEdmPackage(DEFAULT_EDM_PACKAGE)
    		.setNameSpace(NAME_SPACE)
    		.setServiceRoot(SERVICE_ROOT)
    		.initialize();
    	
    	return provider;
    }

    @Bean
    public ServletRegistrationBean dispatcherServletRegistration() {
    	ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet, "/odata/appexample.svc/*");
    	return registration;
    }
    
}
