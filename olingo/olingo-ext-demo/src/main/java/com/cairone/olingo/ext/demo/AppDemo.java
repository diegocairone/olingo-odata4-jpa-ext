package com.cairone.olingo.ext.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class AppDemo extends SpringBootServletInitializer
{
	private static Class<AppDemo> applicationClass = AppDemo.class;
	
    public static void main( String[] args ) {
    	SpringApplication.run(AppDemo.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    	return application.sources(applicationClass);
    }
}
