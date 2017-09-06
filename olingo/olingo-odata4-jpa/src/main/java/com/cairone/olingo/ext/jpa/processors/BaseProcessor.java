package com.cairone.olingo.ext.jpa.processors;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.processor.Processor;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.OdataEnum;
import com.cairone.olingo.ext.jpa.utilities.Util;

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
		
		if(object == null) return null;
		
		Entity entity = new Entity();
		Class<?> clazz = object.getClass();
		
		com.cairone.olingo.ext.jpa.annotations.EdmEntitySet edmEntitySet = clazz.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class);
		com.cairone.olingo.ext.jpa.annotations.EdmEntity edmEntity = clazz.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntity.class);
		
		if(edmEntitySet == null) {
			throw new ODataApplicationException(String.format("Class %s is missing @EdmEntitySet annotation", clazz.getName()), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		if(edmEntity == null) {
			throw new ODataApplicationException(String.format("Class %s is missing @EdmEntity annotation", clazz.getName()), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
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
	            	
	            	} else if(value instanceof BigDecimal) {
	            		
	            		BigDecimal bigDecimalValue = (BigDecimal) value;
	            		entity.addProperty(new Property(null, name, ValueType.PRIMITIVE, bigDecimalValue));
	            		
	            	} else if(value.getClass().isEnum()) {
	            		
	            		OdataEnum<?> odataEnum = (OdataEnum<?>) value;
	            		entity.addProperty(new Property(null, name, ValueType.ENUM, odataEnum.getOrdinal()));
	            		
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

    					if(objects != null) {
	    					for(Object item : objects) {
	    						Entity expandEntity = writeEntity(item, null);
	    						data.getEntities().add(expandEntity);
	    					}
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
		
		String entityID = Util.formatEntityID(keyValues);
		try {
			entity.setId(new URI(edmEntitySetName + entityID));
		} catch (URISyntaxException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
				
		return entity;
	}
	
	protected Object writeObject(Class<?> clazz, Entity entity) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		if(clazz == null || entity == null) {
			return null;
		}
		
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
    					
    					Method setValor = fldClazz.getMethod("setOrdinal", Integer.TYPE);
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
					
					Link link = entity.getNavigationLink(propertyName);
					
					if(link != null) {
						
						EntityCollection entityCollection = link.getInlineEntitySet();
						
						if(entityCollection != null) {
							List<Entity> entities = entityCollection.getEntities();
							
							ParameterizedType listType = (ParameterizedType) fld.getGenericType();
							Type type = listType.getActualTypeArguments()[0];
					        Class<?> inlineClazz = (Class<?>) type;
					        
					        ArrayList<Object> inlineObjectCollection = new ArrayList<Object>();
							
							for(Entity inlineEntity : entities) {
								Object inlineObject = writeObject(inlineClazz, inlineEntity);
								if(inlineObject != null) inlineObjectCollection.add(inlineObject);
							}
							
							fld.setAccessible(true);
							fld.set(object, inlineObjectCollection);
						}
					}
					
				} else {
					
					fld.setAccessible(true);
					
					com.cairone.olingo.ext.jpa.annotations.EdmEntitySet targetEdmEntitySet = fieldClass.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmEntitySet.class);
					String targetEntitySetName = targetEdmEntitySet.value();
					Class<?> cl = entitySetMap.get(targetEntitySetName);
					
					Link link = entity.getNavigationLink(propertyName);
					if(link != null) {
						Object navpropField = writeObject(cl, link.getInlineEntity());
						if(navpropField != null) fld.set(object, navpropField);
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
	
	protected Object convertEdmType(String edmType, String value) {
		
		if(edmType.equals("Edm.Int32")) {
			return Integer.parseInt(value);
		} else if(edmType.equals("Edm.Int64")) {
			return Long.parseLong(value);
		} else if(edmType.equals("Edm.Date") && value.matches(REGEX_DATE_FORMAT)) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(REGEX_DATE_FORMAT);
			LocalDate date = LocalDate.parse(value, formatter);
			return date;
		} else if(edmType.equals("Edm.Decimal")) {
			return BigDecimal.valueOf(Double.valueOf(value));
		} else {
			return value;
		}
	}

	protected Entity createEntity(Object createdObject) throws ODataApplicationException {
		
		Entity createdEntity = new Entity();
		
		EdmEntity edmEntity = createdObject.getClass().getAnnotation(EdmEntity.class);
    	String[] keys = edmEntity.key();
    	Map<String, Object> keyValues = Arrays.asList(keys)
    		.stream()
    		.collect(Collectors.toMap(x -> x, x -> x));
    	
    	try
    	{
			for(Field fld : createdObject.getClass().getDeclaredFields()) {
	
	    		com.cairone.olingo.ext.jpa.annotations.EdmProperty edmProperty = fld.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
				
	            if (edmProperty != null) {
	            	
	            	fld.setAccessible(true);
	            	
	            	String name = edmProperty.name().isEmpty() ? fld.getName() : edmProperty.name();
	            	Object value = fld.get(createdObject);
	            	
	            	if(value != null) {
		            		
		            	if(value instanceof LocalDate) {
		            		
		            		LocalDate localDateValue = (LocalDate) value;
		            		createdEntity.addProperty(new Property(null, name, ValueType.PRIMITIVE, GregorianCalendar.from(localDateValue.atStartOfDay(ZoneId.systemDefault()))));
		            	
		            	} else if(value.getClass().isEnum()) {
		            		
		            		Class<?> fldClazz = fld.getType();
		            		Method getValor = fldClazz.getMethod("getOrdinal");
	    					Enum<?>[] enums = (Enum<?>[]) fldClazz.getEnumConstants();
	    					
	    					Object rvValue = getValor.invoke(value);
	    					
	    					for(Enum<?> enumeration : enums) {
	    						Object rv = getValor.invoke(enumeration);
	    						if(rvValue.equals(rv)) {
		    						createdEntity.addProperty(new Property(null, name, ValueType.ENUM, rv));
		                    		break;
	    						}
	    					}
		            	} else {
		            		createdEntity.addProperty(new Property(null, name, ValueType.PRIMITIVE, value));
		            	}
	            	}
	            	
	            	if(keyValues.containsKey(name)) {
	            		keyValues.put(name, value);
	            	}
	            }
			}
    	} catch(SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
    		throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
    	}
    	
		return createdEntity;
	}
	
	protected void writeNavLinksFromNavBindings(Entity requestEntity, Map<String, DataSource> dataSourceMap, String rawBaseUri) throws ODataApplicationException {

		List<Link> navigationBindings = requestEntity.getNavigationBindings();
		
    	if(navigationBindings != null && navigationBindings.size() > 0) {
    		
    		for(Link link : navigationBindings) {
    			    							
				Link navLink = new Link();
				navLink.setTitle(link.getTitle());
				requestEntity.getNavigationLinks().add(navLink);
				
				
			    if(link.getBindingLinks().isEmpty()) {
			    	
			    	String bindingLink = link.getBindingLink();
			    	
			    	try {
			    		
				    	UriResourceEntitySet targetUriResourceEntitySet = 
			    				odata.createUriHelper().parseEntityId(serviceMetadata.getEdm(), bindingLink, rawBaseUri);
				    	
				    	EdmEntitySet targetEntitySet = targetUriResourceEntitySet.getEntitySet();
				    	
		    			List<UriParameter> keyPredicates = targetUriResourceEntitySet.getKeyPredicates();
		    		    Map<String, UriParameter> keyPredicateMap = keyPredicates
		    					.stream()
		    					.collect(Collectors.toMap(UriParameter::getName, x -> x));

					    DataSource targetDataSource = dataSourceMap.get(targetEntitySet.getName());

						if(targetDataSource == null) {
							throw new ODataApplicationException(String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", targetEntitySet.getName()), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
						}

	    				Object targetObject = targetDataSource.readFromKey(keyPredicateMap, null, null);
	    				
	    				if(targetObject == null) {
	    					throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    				}
	    				
	    				Entity targetEntity = writeEntity(targetObject, null);
	    				navLink.setInlineEntity(targetEntity);
				    	
				    	
	    			} catch (ODataException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
	    				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	    			}
			    	
			    } else {
			    	
			    	EntityCollection entityCollection = navLink.getInlineEntitySet();
			    	
			    	if(entityCollection == null) {
			    		entityCollection = new EntityCollection();
			    		navLink.setInlineEntitySet(entityCollection);
			    	}
			    				    	
			    	for(final String bindingLink : link.getBindingLinks()) {
			    		
			    		try {
			    			
				    		UriResourceEntitySet targetUriResourceEntitySet = 
				    				odata.createUriHelper().parseEntityId(serviceMetadata.getEdm(), bindingLink, rawBaseUri);

					    	EdmEntitySet targetEntitySet = targetUriResourceEntitySet.getEntitySet();
					    		
			    			List<UriParameter> keyPredicates = targetUriResourceEntitySet.getKeyPredicates();
			    		    Map<String, UriParameter> keyPredicateMap = keyPredicates
			    					.stream()
			    					.collect(Collectors.toMap(UriParameter::getName, x -> x));
			    		    

						    DataSource targetDataSource = dataSourceMap.get(targetEntitySet.getName());

							if(targetDataSource == null) {
								throw new ODataApplicationException(String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", targetEntitySet.getName()), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
							}

		    				Object targetObject = targetDataSource.readFromKey(keyPredicateMap, null, null);
		    				
		    				if(targetObject == null) {
		    					throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		    				}
		    				
		    				Entity targetEntity = writeEntity(targetObject, null);
		    				entityCollection.getEntities().add(targetEntity);
				    				
		    			} catch (ODataException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
		    				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		    			}
			    	}
			    }
    		}
    	}
	}
	
	@Deprecated
	protected void writeNavLinksFromNavBindings(EdmEntitySet edmEntitySet, Entity requestEntity, Map<String, DataSource> dataSourceMap, String rawBaseUri) throws ODataApplicationException {

		List<Link> navigationBindings = requestEntity.getNavigationBindings();
		
    	if(navigationBindings != null && navigationBindings.size() > 0) {
    		
    		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
    		    		
    		for(Link link : navigationBindings) {
    			
    			final EdmNavigationProperty edmNavigationProperty = edmEntityType.getNavigationProperty(link.getTitle());
			    final EdmEntitySet targetEntitySet = (EdmEntitySet) edmEntitySet.getRelatedBindingTarget(link.getTitle());
			    
			    DataSource targetDataSource = dataSourceMap.get(targetEntitySet.getName());

				if(targetDataSource == null) {
					throw new ODataApplicationException(String.format("DATASOURCE PROVIDER FOR %s NOT FOUND", targetEntitySet.getName()), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
				
				Link navLink = requestEntity.getNavigationLink(link.getTitle());
				if(navLink == null) {
					navLink = new Link();
					navLink.setTitle(link.getTitle());
					requestEntity.getNavigationLinks().add(navLink);
				}
				
			    if(edmNavigationProperty.isCollection() && link.getBindingLinks() != null) {
			    	
			    	EntityCollection entityCollection = navLink.getInlineEntitySet();
			    	
			    	if(entityCollection == null) {
			    		entityCollection = new EntityCollection();
			    		navLink.setInlineEntitySet(entityCollection);
			    	}
			    				    	
			    	for(final String bindingLink : link.getBindingLinks()) {
			    		
			    		try {
			    			
				    		UriResourceEntitySet targetUriResourceEntitySet = 
				    				odata.createUriHelper().parseEntityId(serviceMetadata.getEdm(), bindingLink, rawBaseUri);
				    		
				    		if(targetUriResourceEntitySet.getEntitySet().getName().equals(targetEntitySet.getName())) {
				    			
				    			List<UriParameter> keyPredicates = targetUriResourceEntitySet.getKeyPredicates();
				    		    Map<String, UriParameter> keyPredicateMap = keyPredicates
				    					.stream()
				    					.collect(Collectors.toMap(UriParameter::getName, x -> x));
				    		    
				    			
				    				Object targetObject = targetDataSource.readFromKey(keyPredicateMap, null, null);
				    				
				    				if(targetObject == null) {
				    					throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
				    				}
				    				
				    				Entity targetEntity = writeEntity(targetObject, null);
				    				entityCollection.getEntities().add(targetEntity);
				    			
				    		}
				    			
		    			} catch (ODataException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
		    				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		    			}
			    	}
			    } else if(!edmNavigationProperty.isCollection() && link.getBindingLink() != null) {
			    	
			    	String bindingLink = link.getBindingLink();
			    	
			    	try {
			    		
				    	UriResourceEntitySet targetUriResourceEntitySet = 
			    				odata.createUriHelper().parseEntityId(serviceMetadata.getEdm(), bindingLink, rawBaseUri);
				    	
				    	if(targetUriResourceEntitySet.getEntitySet().getName().equals(targetEntitySet.getName())) {
	
			    			List<UriParameter> keyPredicates = targetUriResourceEntitySet.getKeyPredicates();
			    		    Map<String, UriParameter> keyPredicateMap = keyPredicates
			    					.stream()
			    					.collect(Collectors.toMap(UriParameter::getName, x -> x));
			    		    
			    				Object targetObject = targetDataSource.readFromKey(keyPredicateMap, null, null);
			    				
			    				if(targetObject == null) {
			    					throw new ODataApplicationException("LA ENTIDAD SOLICITADA NO EXISTE", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
			    				}
			    				
			    				Entity targetEntity = writeEntity(targetObject, null);
			    				navLink.setInlineEntity(targetEntity);
				    	}
				    	
	    			} catch (ODataException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
	    				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	    			}
			    }
    		}
    	}
	}
}
