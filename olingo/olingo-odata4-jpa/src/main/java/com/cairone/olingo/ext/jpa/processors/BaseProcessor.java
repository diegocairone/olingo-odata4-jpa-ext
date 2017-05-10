package com.cairone.olingo.ext.jpa.processors;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.processor.Processor;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.cairone.olingo.ext.jpa.annotations.EdmEntity;

public class BaseProcessor implements Processor {

	private static String REGEX_DATE_FORMAT = "\\d{4}-\\d{2}-\\d{2}";
	
	protected String SERVICE_ROOT = null;
	protected String DEFAULT_EDM_PACKAGE = null;
	
	protected OData odata;
	protected ServiceMetadata serviceMetadata;
	
	protected Map<String, Class<?>> entitySetMap = new HashMap<>();
	protected Map<String, String> entityTypeMap = new HashMap<>();
	
	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}
	
	public BaseProcessor initialize(ApplicationContext context) throws ODataApplicationException {
		
		ClassPathScanningCandidateComponentProvider provider = createComponentScanner(Arrays.asList(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class));
		Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(DEFAULT_EDM_PACKAGE);
		
		try {
			for(BeanDefinition beanDef : beanDefinitions) {
				Class<?> cl = Class.forName(beanDef.getBeanClassName());
				
				com.cairone.olingo.ext.jpa.annotations.EdmEntitySet edmEntitySet = cl.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class);
				EdmEntity edmEntity = cl.getAnnotation(EdmEntity.class);
				
				if(edmEntitySet != null) {
					entitySetMap.put(edmEntitySet.value(), cl);
					
					if(edmEntity != null) {
						entityTypeMap.put(edmEntity.name(), edmEntitySet.value());
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		return this;
	}

	public String getServiceRoot() {
		return SERVICE_ROOT;
	}

	public BaseProcessor setServiceRoot(String ServiceRoot) {
		SERVICE_ROOT = ServiceRoot;
		return this;
	}

	public String getDefaultEdmPackage() {
		return DEFAULT_EDM_PACKAGE;
	}

	public BaseProcessor setDefaultEdmPackage(String DefaultEdmPackage) {
		DEFAULT_EDM_PACKAGE = DefaultEdmPackage;
		return this;
	}

	protected Entity writeEntity(Object object, ExpandOption expandOption) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, ODataApplicationException {
		
		Entity entity = new Entity();
		Class<?> clazz = object.getClass();
		
		com.cairone.olingo.ext.jpa.annotations.EdmEntitySet edmEntitySet = clazz.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class);
		com.cairone.olingo.ext.jpa.annotations.EdmEntity edmEntity = clazz.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntity.class);
		
		String edmEntitySetName = edmEntitySet.value().isEmpty() ? clazz.getSimpleName() : edmEntitySet.value();
		
		String[] keys = edmEntity.key();
    	Map<String, Object> keyValues = Arrays.asList(keys)
    		.stream()
    		.collect(Collectors.toMap(x -> x, x -> x));
    	
    	Map<String, EdmNavigationProperty> edmNavigationPropertyMap = new HashMap<String, EdmNavigationProperty>();
    	if(expandOption != null && !expandOption.getExpandItems().isEmpty()) {
    		expandOption.getExpandItems().forEach(expandItem -> {
    			UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
    			if(uriResource instanceof UriResourceNavigation) {
    				EdmNavigationProperty edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
    				String navPropName = edmNavigationProperty.getName();
    				edmNavigationPropertyMap.put(navPropName, edmNavigationProperty);
    			}
    		});
    	}
		
		for(Field fld : object.getClass().getDeclaredFields()) {

    		com.cairone.olingo.ext.jpa.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
			
            if (edmProperty != null) {
            	
            	fld.setAccessible(true);
            	
            	String name = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
            	Object value = fld.get(object);
            	
            	if(value != null) {
            		
	            	if(value instanceof LocalDate) {
	            		
	            		LocalDate localDateValue = (LocalDate) value;
	            		entity.addProperty(new Property(null, name, ValueType.PRIMITIVE, GregorianCalendar.from(localDateValue.atStartOfDay(ZoneId.systemDefault()))));
	            	
	            	} else if(value.getClass().isEnum()) {
	            		
	            		Class<?> fldClazz = fld.getType();
	            		Method getValor = fldClazz.getMethod("getValor");
						Enum<?>[] enums = (Enum<?>[]) fldClazz.getEnumConstants();
						
						Object rvValue = getValor.invoke(value);
						
						for(Enum<?> enumeration : enums) {
							Object rv = getValor.invoke(enumeration);
							if(rvValue.equals(rv)) {
	    						entity.addProperty(new Property(null, name, ValueType.ENUM, rv));
	                    		break;
							}
						}
	            	} else {
	            		entity.addProperty(new Property(null, name, ValueType.PRIMITIVE, value));
	            	}
	            	
	            	if(keyValues.containsKey(name)) {
	            		keyValues.put(name, value);
	            	}
            	}
            }
            
            com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty edmNavigationProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty.class);
			
            if(edmNavigationProperty != null) {
            	
            	String navigationPropertyName = edmNavigationProperty.name().isEmpty() ? fld.getName() : edmNavigationProperty.name();
            	EdmNavigationProperty navigationProperty = edmNavigationPropertyMap.get(navigationPropertyName);
            	
            	if(navigationProperty != null) {

            		fld.setAccessible(true);
            		
            		Class<?> fieldClass = fld.getType();
            		Object inlineEntity = fld.get(object);

            		Link link = new Link();
					link.setTitle(navigationPropertyName);
					
    				if(Collection.class.isAssignableFrom(fieldClass)) {
    					
    					EntityCollection data = new EntityCollection();
    					
    					@SuppressWarnings("unchecked")
						Collection<Object> objects = (Collection<Object>) inlineEntity;
    					
    					for(Object item : objects) {
    						Entity expandEntity = writeEntity(item, null);
    						data.getEntities().add(expandEntity);
    					}
    					
    					link.setInlineEntitySet(data);
    					
    				} else {
    					Entity expandEntity = writeEntity(inlineEntity, null);
    					link.setInlineEntity(expandEntity);
    				}

					entity.getNavigationLinks().add(link);
            	}
            }
		}
		
		String entityID = keyValues.entrySet().stream().map(Entry::toString).collect(Collectors.joining(",", "(", ")"));
		try {
			entity.setId(new URI(edmEntitySetName + entityID));
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
				
		return entity;
	}
	
	protected Object writeObject(Class<?> clazz, Entity entity) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Constructor<?> constructor = clazz.getConstructor();
		Object object = constructor.newInstance();
		
		for (Field fld : clazz.getDeclaredFields()) {
			
			com.cairone.olingo.ext.jpa.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
			
			if (edmProperty != null) {
            	
            	String propertyName = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
            	Property property = entity.getProperty(propertyName);
            	
            	if(property != null) {
            		
    				Class<?> fldClazz = fld.getType();
    				com.cairone.olingo.ext.jpa.annotations.EdmEnum edmEnum = fldClazz.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEnum.class);
    				
    				if(edmEnum != null) {
    					
    					Method setValor = fldClazz.getMethod("setValor", Integer.TYPE);
    					Enum<?>[] enums = (Enum<?>[]) fldClazz.getEnumConstants();
    					
    					for(Enum<?> enumeration : enums) {
    						Object rv = setValor.invoke(enumeration, property.asEnum());
    						fld.setAccessible(true);
                    		fld.set(object, rv);
                    		break;
    					}
    					
    				} else {
	    				
	            		if(fld.getType().isAssignableFrom(LocalDate.class) && property.getValue() instanceof GregorianCalendar) {
	
	                		GregorianCalendar cal = (GregorianCalendar) property.getValue();
	                		
	                		fld.setAccessible(true);
	                		fld.set(object, cal.toZonedDateTime().toLocalDate());
	                		
	            		} else {
	
	                		fld.setAccessible(true);
	                		fld.set(object, property.getValue());
	            		}
    				}
            	}
            }
			
			com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty edmNavigationProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty.class);
			
        	if(edmNavigationProperty != null) {
        		
        		String propertyName = edmNavigationProperty.name().isEmpty() ? fld.getName() : edmNavigationProperty.name();

        		Class<?> fieldClass = fld.getType();

				if(Collection.class.isAssignableFrom(fieldClass)) {
					//FIXME
				} else {

					fld.setAccessible(true);
//					Object navpropField = fld.get(object);

					com.cairone.olingo.ext.jpa.annotations.EdmEntitySet targetEdmEntitySet = fieldClass.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class);
					String targetEntitySetName = targetEdmEntitySet.value();
					Class<?> cl = entitySetMap.get(targetEntitySetName);

//					if(navpropField == null) {
//						Constructor<?> c = cl.getConstructor();
//    					navpropField = c.newInstance();
//    					fld.set(object, navpropField);
//					}
					
					Link link = entity.getNavigationLink(propertyName);
					if(link != null) {
						Object navpropField = writeObject(cl, link.getInlineEntity());
						fld.set(object, navpropField);
					}
				}
        	}
		}
		
		return object;
	}

	protected Map<String, Parameter> readActionParameters(final org.apache.olingo.commons.api.edm.EdmAction action, final InputStream body, final ContentType requestFormat) throws ODataApplicationException, DeserializerException {
		if (action.getParameterNames().size() - (action.isBound() ? 1 : 0) > 0) {
			return odata.createDeserializer(requestFormat).actionParameters(body, action).getActionParameters();
		}
		return Collections.<String, Parameter> emptyMap();
	}

	protected ClassPathScanningCandidateComponentProvider createComponentScanner(Iterable<Class<? extends Annotation>> annotationTypes) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		for(Class<? extends Annotation> annotationType : annotationTypes) provider.addIncludeFilter(new AnnotationTypeFilter(annotationType));
		return provider;
    }

	protected String inferEdmType(Field field) {
		
		if(field.getType().isAssignableFrom(Integer.class)) {
			return "Edm.Int32";
		} else if (field.getType().isAssignableFrom(Long.class)) {
			return "Edm.Int64";
		} else if (field.getType().isAssignableFrom(LocalDate.class)) {
			return "Edm.Date";
		} else if (field.getType().isAssignableFrom(Boolean.class)) {
			return "Edm.Boolean";
		} else if (field.getType().isAssignableFrom(BigDecimal.class)) {
			return "Edm.Decimal";
		}
		
		return "Edm.String";
	}
	
	protected Object convertEdmType(String edmType, String value) {
		
		if(edmType.equals("Edm.Int32")) {
			return Integer.parseInt(value);
		} else if(edmType.equals("Edm.Int64")) {
			return Long.parseLong(value);
		} else if(edmType.equals("Edm.Date") && value.matches(REGEX_DATE_FORMAT)) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(REGEX_DATE_FORMAT);
			LocalDate date = LocalDate.parse(value, formatter);
			return date;
		} else {
			return value;
		}
	}
}
