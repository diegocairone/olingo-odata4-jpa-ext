package com.cairone.olingo.ext.demo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class AppDemo extends SpringBootServletInitializer implements CommandLineRunner
{
	private static Class<AppDemo> applicationClass = AppDemo.class;
	protected static final Logger LOG = LoggerFactory.getLogger(AppDemo.class);
	
	@PersistenceContext private EntityManager em = null;
	
    public static void main( String[] args ) {
    	SpringApplication.run(AppDemo.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    	return application.sources(applicationClass);
    }

	@Override
	public void run(String... args) throws Exception {
		/*
		String queryString = "SELECT e FROM UserEntity e LEFT JOIN FETCH e.person e1 LEFT JOIN FETCH e1.form e2";
		String queryString = "SELECT e FROM UserEntity e";
		LOG.info(">> QUERY: {}", queryString);
		
		Query query = em.createQuery(queryString, UserEntity.class);
		
		@SuppressWarnings("unchecked")
		List<UserEntity> userEntities = query.getResultList();
		
		userEntities.forEach(e -> {
			LOG.info("USER: {}", e);
		});*/
	}
}
