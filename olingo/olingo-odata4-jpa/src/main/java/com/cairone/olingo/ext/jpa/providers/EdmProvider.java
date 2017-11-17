package com.cairone.olingo.ext.jpa.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.cairone.olingo.ext.jpa.annotations.EdmAction;
import com.cairone.olingo.ext.jpa.annotations.EdmActionImport;
import com.cairone.olingo.ext.jpa.annotations.EdmComplex;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmEnum;
import com.cairone.olingo.ext.jpa.annotations.EdmFunction;
import com.cairone.olingo.ext.jpa.annotations.EdmFunctionImport;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;

public class EdmProvider extends CsdlAbstractEdmProvider {

	private static final Logger LOG = LoggerFactory.getLogger(EdmProvider.class);
	
	private String NAME_SPACE = null;
	private String CONTAINER_NAME = null;
	private String SERVICE_ROOT = null;
	private String DEFAULT_EDM_PACKAGE = null;
	
	private HashMap<String, Class<?>> entitySetsMap = new HashMap<String, Class<?>>();
	private HashMap<String, Class<?>> enumsMap = new HashMap<String, Class<?>>();
	private HashMap<String, Class<?>> actionsMap = new HashMap<String, Class<?>>();
	private HashMap<String, Class<?>> actionImportsMap = new HashMap<String, Class<?>>();
	private HashMap<String, Class<?>> functionsMap = new HashMap<String, Class<?>>();
	private HashMap<String, Class<?>> functionImportsMap = new HashMap<String, Class<?>>();
	private HashMap<String, Class<?>> complexTypesMap = new HashMap<String, Class<?>>();
	private HashMap<String, String> entityTypesMap = new HashMap<>();
	
	public EdmProvider initialize() throws ODataApplicationException {

		ClassPathScanningCandidateComponentProvider provider = createComponentScanner(Arrays.asList(
				EdmEntitySet.class, 
				EdmEnum.class, 
				EdmAction.class, 
				EdmActionImport.class,
				EdmFunction.class,
				EdmFunctionImport.class,
				EdmComplex.class));
		
		Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(DEFAULT_EDM_PACKAGE);
		
		try {
			for(BeanDefinition beanDef : beanDefinitions) {
				Class<?> cl = Class.forName(beanDef.getBeanClassName());
				
				EdmEntitySet edmEntitySet = cl.getAnnotation(EdmEntitySet.class);
				EdmEnum edmEnum = cl.getAnnotation(EdmEnum.class);
				EdmAction edmAction = cl.getAnnotation(EdmAction.class);
				EdmActionImport edmActionImport = cl.getAnnotation(EdmActionImport.class);
				EdmFunction edmFunction = cl.getAnnotation(EdmFunction.class);
				EdmFunctionImport edmFunctionImport = cl.getAnnotation(EdmFunctionImport.class);
				EdmComplex edmComplex = cl.getAnnotation(EdmComplex.class);
				
				if(edmEntitySet != null) {
					EdmEntity edmEntity = cl.getAnnotation(EdmEntity.class);
					
					if(edmEntitySet.includedInServiceDocument()) {
						entitySetsMap.put(edmEntitySet.value(), cl);
						entityTypesMap.put(edmEntity.name(), edmEntitySet.value());
					}
				}
				
				if(edmEnum != null) {
					String name = edmEnum.name().isEmpty() ? cl.getSimpleName() : edmEnum.name();
					enumsMap.put(name, cl);
				}
				
				if(edmAction != null) {
					String name = edmAction.name().isEmpty() ? cl.getSimpleName() : edmAction.name();
					actionsMap.put(name, cl);
				}

				if(edmActionImport != null) {
					String name = edmActionImport.name().isEmpty() ? cl.getSimpleName() : edmActionImport.name();
					actionImportsMap.put(name, cl);
				}

				if(edmFunction != null) {
					String name = edmFunction.name().isEmpty() ? cl.getSimpleName() : edmFunction.name();
					functionsMap.put(name, cl);
				}

				if(edmFunctionImport != null) {
					String name = edmFunctionImport.name().isEmpty() ? cl.getSimpleName() : edmFunctionImport.name();
					functionImportsMap.put(name, cl);
				}
				
				if(edmComplex != null) {
					String name = edmComplex.name().isEmpty() ? cl.getSimpleName() : edmComplex.name();
					complexTypesMap.put(name, cl);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
		}
		
		return this;
	}
	
	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {
		
		// create Schema
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAME_SPACE);

		// add EntityTypes
		List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		
		for(Map.Entry<String, Class<?>> entry : entitySetsMap.entrySet()) {
			Class<?> clazz = entry.getValue();
			EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
			String namespace = edmEntity.namespace().isEmpty() ? NAME_SPACE : edmEntity.namespace();
			String name = edmEntity.name().isEmpty() ? clazz.getSimpleName() : edmEntity.name();
			entityTypes.add(getEntityType(getFullQualifiedName(namespace, name)));
		}
		
		entityTypes.sort(new Comparator<CsdlEntityType>() {
			@Override
			public int compare(CsdlEntityType o1, CsdlEntityType o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		schema.setEntityTypes(entityTypes);
		
		// add EnumTypes
		List<CsdlEnumType> enumTypes = new ArrayList<CsdlEnumType>();
		
		for(Map.Entry<String, Class<?>> entry : enumsMap.entrySet()) {
			Class<?> clazz = entry.getValue();
			EdmEnum edmEnum = clazz.getAnnotation(EdmEnum.class);
			String namespace = edmEnum.namespace().isEmpty() ? NAME_SPACE : edmEnum.namespace();
			String name = edmEnum.name().isEmpty() ? clazz.getSimpleName() : edmEnum.name();
			enumTypes.add(getEnumType(getFullQualifiedName(namespace, name)));
		}
		
		schema.setEnumTypes(enumTypes);
		
		// add actions
		List<CsdlAction> actions = new ArrayList<CsdlAction>();

		for(Map.Entry<String, Class<?>> entry : actionsMap.entrySet()) {
			Class<?> clazz = entry.getValue();
			EdmAction edmAction = clazz.getAnnotation(EdmAction.class);
			String namespace = edmAction.namespace().isEmpty() ? NAME_SPACE : edmAction.namespace();
			String name = edmAction.name().isEmpty() ? clazz.getSimpleName() : edmAction.name();
			actions.add(getAction(getFullQualifiedName(namespace, name)));
		}
		
		schema.setActions(actions);

		// add functions
		List<CsdlFunction> functions = new ArrayList<CsdlFunction>();

		for(Map.Entry<String, Class<?>> entry : functionsMap.entrySet()) {
			Class<?> clazz = entry.getValue();
			EdmFunction edmFunction = clazz.getAnnotation(EdmFunction.class);
			String namespace = edmFunction.namespace().isEmpty() ? NAME_SPACE : edmFunction.namespace();
			String name = edmFunction.name().isEmpty() ? clazz.getSimpleName() : edmFunction.name();
			functions.add(getFunction(getFullQualifiedName(namespace, name)));
		}
		
		schema.setFunctions(functions);
		
		// add complex types
		List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();

		for(Map.Entry<String, Class<?>> entry : complexTypesMap.entrySet()) {
			Class<?> clazz = entry.getValue();
			EdmComplex edmComplex = clazz.getAnnotation(EdmComplex.class);
			String namespace = edmComplex.namespace().isEmpty() ? NAME_SPACE : edmComplex.namespace();
			String name = edmComplex.name().isEmpty() ? clazz.getSimpleName() : edmComplex.name();
			complexTypes.add(getComplexType(getFullQualifiedName(namespace, name)));
		}
		
		schema.setComplexTypes(complexTypes);
		
		// add EntityContainer
		schema.setEntityContainer(getEntityContainer());

		// finally
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		schemas.add(schema);

		return schemas;
	}

	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {

		FullQualifiedName CONTAINER = getFullQualifiedName(CONTAINER_NAME);
		
		List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
		
		for(Map.Entry<String, Class<?>> entry : entitySetsMap.entrySet()) {
			String entitySet = entry.getKey();
			entitySets.add(getEntitySet(CONTAINER, entitySet));
		}
		
		entitySets.sort(new Comparator<CsdlEntitySet>() {
			@Override
			public int compare(CsdlEntitySet o1, CsdlEntitySet o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		// Create action imports
		List<CsdlActionImport> actionImports = new ArrayList<CsdlActionImport>();
		
		for(Map.Entry<String, Class<?>> entry : actionImportsMap.entrySet()) {
			String actionImport = entry.getKey();
			actionImports.add(getActionImport(CONTAINER, actionImport));
		}

		// Create function imports
		List<CsdlFunctionImport> functionImports = new ArrayList<CsdlFunctionImport>();
		
		for(Map.Entry<String, Class<?>> entry : functionImportsMap.entrySet()) {
			String functionImport = entry.getKey();
			functionImports.add(getFunctionImport(CONTAINER, functionImport));
		}
		
		// create EntityContainer
		CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(CONTAINER_NAME);
		entityContainer.setEntitySets(entitySets);
		entityContainer.setActionImports(actionImports);
		entityContainer.setFunctionImports(functionImports);

		return entityContainer;
	}

	@Override
	public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
		
		// This method is invoked when displaying the Service Document at e.g. http://localhost:8080/DemoService/DemoService.svc
		
		FullQualifiedName CONTAINER = new FullQualifiedName(NAME_SPACE, CONTAINER_NAME);
		
		if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
			
			CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
	        entityContainerInfo.setContainerName(CONTAINER);
	        return entityContainerInfo;
	    }

	    return null;
	}
	
	@Override
	public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
		
		Class<?> clazz = entitySetsMap.get(entitySetName);
		if(clazz == null) return null;
		
		EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
		
		List<CsdlNavigationPropertyBinding> navigationPropertyBindings = new ArrayList<>();
		
		for(Field fld : clazz.getDeclaredFields()) {
			
			EdmNavigationProperty navigationProperty = fld.getAnnotation(EdmNavigationProperty.class);
			if(navigationProperty != null) {
				
				String path = navigationProperty.name();
				String target = null;
				
				Class<?> fieldClass = fld.getType();
				
				if(Collection.class.isAssignableFrom(fieldClass)) {
					Type type = fld.getGenericType();
					if (type instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType) type;
						for(Type t : pt.getActualTypeArguments()) {
							Class<?> cl = (Class<?>) t;
							EdmEntitySet edmEntitySetInField = cl.getAnnotation(EdmEntitySet.class);
							target = edmEntitySetInField.value();
						}
					}
				} else {
					EdmEntitySet edmEntitySet = fieldClass.getAnnotation(EdmEntitySet.class);
					target = edmEntitySet.value();
				}
				
				CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding().setPath(path).setTarget(target);
				navigationPropertyBindings.add(navPropBinding);
			}
		}
		
		CsdlEntitySet entitySet = new CsdlEntitySet()
			.setName(entitySetName)
			.setType(getFullQualifiedName(edmEntity.namespace(), edmEntity.name()))
			.setNavigationPropertyBindings(navigationPropertyBindings);
		
		return entitySet;
	}
	
	@Override
	public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) throws ODataException {

		String complexTypeNameString = complexTypeName.getName();
		Class<?> clazz = complexTypesMap.get(complexTypeNameString);
		
		if(clazz == null) return null;

		EdmComplex edmComplex = clazz.getAnnotation(EdmComplex.class);

		Field[] fields = clazz.getDeclaredFields();
		
		List<CsdlProperty> csdlProperties = getCsdlProperties(fields);
		List<CsdlNavigationProperty> csdlNavigationProperties = getCsdlNavigationProperties(fields);
		
		CsdlComplexType complexType = new CsdlComplexType()
				.setName(edmComplex.name())
				.setProperties(csdlProperties)
				.setNavigationProperties(csdlNavigationProperties)
				.setOpenType(edmComplex.open());
		
		return complexType;		
	}

	@Override
	public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) throws ODataException {
		
		Class<?> clazz = actionImportsMap.get(actionImportName);
		if(clazz == null) return null;
		
		EdmActionImport edmActionImport = clazz.getAnnotation(EdmActionImport.class);
		
		CsdlActionImport csdlActionImport = new CsdlActionImport()
			.setName(actionImportName)
			.setEntitySet(edmActionImport.entitySet())
			.setAction(getFullQualifiedName(edmActionImport.namespace(), edmActionImport.action()));
		
		return csdlActionImport;
	}

	@Override
	public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) throws ODataException {

		Class<?> clazz = functionImportsMap.get(functionImportName);
		if(clazz == null) return null;
		
		EdmFunctionImport edmFunctionImport = clazz.getAnnotation(EdmFunctionImport.class);
		
		CsdlFunctionImport csdlFunctionImport = new CsdlFunctionImport()
			.setName(functionImportName)
			.setEntitySet(edmFunctionImport.entitySet())
			.setFunction(getFullQualifiedName(edmFunctionImport.namespace(), edmFunctionImport.function()));
		
		return csdlFunctionImport;
	}

	private List<CsdlNavigationProperty> getCsdlNavigationProperties(Field[] fields) {
		
		List<CsdlNavigationProperty> csdlNavigationProperties = new ArrayList<CsdlNavigationProperty>();
		
		for (Field fld : fields) {
			
			EdmNavigationProperty navigationProperty = fld.getAnnotation(EdmNavigationProperty.class);
			if(navigationProperty != null) {
				
				String navigationPropertyTypeName = navigationProperty.type().isEmpty() ? null : navigationProperty.type();
				boolean isCollection = false;
				
				if(navigationPropertyTypeName == null) {
					Class<?> fieldClass = fld.getType();
					
					if(Collection.class.isAssignableFrom(fieldClass)) {
						isCollection = true;
						Type type = fld.getGenericType();
						if (type instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) type;
							for(Type t : pt.getActualTypeArguments()) {
								Class<?> clazz = (Class<?>) t;
								EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);
								navigationPropertyTypeName = edmEntity == null ? null : edmEntity.name();
							}
						}						
					} else {
						EdmEntity edmEntity = fieldClass.getAnnotation(EdmEntity.class);
						navigationPropertyTypeName = edmEntity == null ? null : edmEntity.name();
					}
				}
				
				CsdlNavigationProperty csdlNavigationProperty = new CsdlNavigationProperty()
			        .setName(navigationProperty.name())
			        .setType(getFullQualifiedName(navigationPropertyTypeName))
			        .setCollection(isCollection)
			        .setNullable(navigationProperty.nullable());
				
				if(!navigationProperty.partner().isEmpty()) {
					csdlNavigationProperty.setPartner(navigationProperty.partner());
				}
				
				csdlNavigationProperties.add(csdlNavigationProperty);
			}
		}
		
		return csdlNavigationProperties;
	}
	
	private List<CsdlProperty> getCsdlProperties(Field[] fields) {
		
		List<CsdlProperty> csdlProperties = new ArrayList<CsdlProperty>();
		
		for (Field fld : fields) {
			
			EdmProperty property = fld.getAnnotation(EdmProperty.class);
			
			if(property != null) {
				
				Class<?> enumClazz = fld.getType();
				EdmComplex[] edmComplexs = enumClazz.getAnnotationsByType(EdmComplex.class);
				boolean isEdmComplex = edmComplexs.length != 0;
				
				String propertyName = property.name().isEmpty() ? fld.getName() : property.name();
				FullQualifiedName propertyType = null;
				
				if(property.type().isEmpty()) {					
					if(fld.getType().isAssignableFrom(Integer.class)) {
						propertyType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(Long.class)) {
						propertyType = EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(String.class)) {
						propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(LocalDate.class)) {
						propertyType = EdmPrimitiveTypeKind.Date.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(LocalDateTime.class)) {
						propertyType = EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(Boolean.class)) {
						propertyType = EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(BigDecimal.class)) {
						propertyType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
					} else if(isEdmComplex) {
						EdmComplex edmComplex = edmComplexs[0];
						String namespace = edmComplex.namespace().isEmpty() ? NAME_SPACE : edmComplex.namespace();
						String name = edmComplex.name().isEmpty() ? enumClazz.getSimpleName() : edmComplex.name();
						propertyType = getFullQualifiedName(namespace, name);
					} else {
						EdmEnum edmEnum = enumClazz.getAnnotation(EdmEnum.class);
						if(edmEnum != null) {
							String namespace = edmEnum.namespace().isEmpty() ? NAME_SPACE : edmEnum.namespace();
							String name = edmEnum.name().isEmpty() ? enumClazz.getSimpleName() : edmEnum.name();
							propertyType = getFullQualifiedName(namespace, name);
						}						
					}
				} else {
					switch(property.type()) {
					case "Edm.Int32":
						propertyType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
						break;
					case "Edm.Int64":
						propertyType = EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
						break;
					case "Edm.String":
						propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName();
						break;
					case "Edm.Date":
						propertyType = EdmPrimitiveTypeKind.Date.getFullQualifiedName();
						break;
					case "Edm.Decimal":
						propertyType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
					}
				}
				
				LOG.debug("CREATING PROPERTY {} [{}]", propertyName, propertyType);
				
				CsdlProperty csdlProperty = new CsdlProperty()
						.setName(propertyName)
						.setType(propertyType)
						.setNullable(property.nullable());
				
				if(propertyType.equals(EdmPrimitiveTypeKind.String.getFullQualifiedName()) && property.maxLength() > 0) csdlProperty.setMaxLength(property.maxLength());
				if(propertyType.equals(EdmPrimitiveTypeKind.Decimal.getFullQualifiedName())) csdlProperty.setScale(property.scale());
				
				csdlProperties.add(csdlProperty);
			}
		}
		
		return csdlProperties;
	}
	
	public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) throws ODataException {
		
		String edmEnumName = enumTypeName.getName();
		Class<?> clazz = enumsMap.get(edmEnumName);
		
		if(clazz == null) {
			return null;
		} else if(clazz.isEnum()) {

			EdmEnum edmEnum = clazz.getAnnotation(EdmEnum.class);
			List<Object> constants = Arrays.asList(clazz.getEnumConstants());
			
			CsdlEnumType enumType = new CsdlEnumType()
				.setName(edmEnumName)
				.setUnderlyingType(edmEnum.underlyingType());
			
			for(Object obj : constants) {
				Enum<?> enumObj = (Enum<?>) obj;
				try {
					String val = String.valueOf(enumObj.ordinal());
					enumType.getMembers().add(new CsdlEnumMember().setName(obj.toString()).setValue(val));
				} catch (SecurityException | IllegalArgumentException e) {
					throw new ODataException(e.getMessage());
				}
			}
			
			return enumType;
		}
		
		throw new ODataException(String.format("%s IS NOT AN ENUMERATION", edmEnumName));
	}

	@Override
	public List<CsdlAction> getActions(FullQualifiedName actionName) throws ODataException {
		CsdlAction csdlAction = getAction(actionName);
		return csdlAction == null ? null : Arrays.asList(csdlAction);
	}

	public CsdlAction getAction(FullQualifiedName actionName) throws ODataException {
		
		String actionNameString = actionName.getName();
		Class<?> clazz = actionsMap.get(actionNameString);
		
		if(clazz == null) return null;
		
		EdmAction edmAction = clazz.getAnnotation(EdmAction.class);
		EdmReturnType edmReturnType = clazz.getAnnotation(EdmReturnType.class);
		
		boolean isCollection = edmReturnType == null ? false : edmReturnType.type().startsWith("Collection");
		
		CsdlAction action = new CsdlAction()
			.setBound(edmAction.isBound())
			.setName(actionName.getName());
		if(edmAction.isBound()) action.setEntitySetPath(edmAction.entitySetPath());
		
		if(edmReturnType != null) {

			CsdlReturnType returnType = new CsdlReturnType()
					.setCollection(isCollection)
					.setNullable(edmReturnType.nullable());

			if(isCollection) {
				returnType.setType(String.format("Collection(%s)", getFullQualifiedName(edmReturnType.type()).toString() ));
			} else {
				returnType.setType(getFullQualifiedName(edmReturnType.type()));
			}
			
			action.setReturnType(returnType);	
		}
		
		if(edmAction.isBound()) {
			
			Class<?> entitySetClass = entitySetsMap.get(edmAction.entitySetPath());
			EdmEntity edmEntity = entitySetClass.getAnnotation(EdmEntity.class);
						
			CsdlParameter csdlParameter = new CsdlParameter()
				.setName(edmAction.entitySetPath())
				.setType(getFullQualifiedName(edmEntity.namespace(), edmEntity.name()));
			
			action.getParameters().add(csdlParameter);
		}
		
		for(Field fld : clazz.getDeclaredFields()) {
			
			EdmParameter parameter = fld.getAnnotation(EdmParameter.class);
			if(parameter != null) {
				
				String parameterName = parameter.name().isEmpty() ? fld.getName() : parameter.name();
				FullQualifiedName parameterType = null;
				boolean fieldIsCollection = false;

				if(parameter.type().isEmpty()) {					
					if(fld.getType().isAssignableFrom(Integer.class)) {
						parameterType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(String.class)) {
						parameterType = EdmPrimitiveTypeKind.String.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(LocalDate.class)) {
						parameterType = EdmPrimitiveTypeKind.Date.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(Boolean.class)) {
						parameterType = EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(BigDecimal.class)) {
						parameterType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(List.class)) {
						fieldIsCollection = true;
						Type genericType = fld.getGenericType();
						if(genericType instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) genericType;
							Class<?> t = (Class<?>) pt.getActualTypeArguments()[0];
							if(t.isAssignableFrom(Integer.class)) {
								parameterType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
							} else if(t.isAssignableFrom(String.class)) {
								parameterType = EdmPrimitiveTypeKind.String.getFullQualifiedName();
							} else if(t.isAssignableFrom(LocalDate.class)) {
								parameterType = EdmPrimitiveTypeKind.Date.getFullQualifiedName();
							} else if(t.isAssignableFrom(Boolean.class)) {
								parameterType = EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
							} else if(t.isAssignableFrom(BigDecimal.class)) {
								parameterType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
							} else {
								EdmEnum edmEnum = t.getAnnotation(EdmEnum.class);
								if(edmEnum != null) {
									String namespace = edmEnum.namespace().isEmpty() ? NAME_SPACE : edmEnum.namespace();
									String name = edmEnum.name().isEmpty() ? t.getSimpleName() : edmEnum.name();
									parameterType = getFullQualifiedName(namespace, name);
								}
								EdmEntity edmEntity = t.getAnnotation(EdmEntity.class);
								if(edmEntity != null) {
									String namespace = edmEntity.namespace().isEmpty() ? NAME_SPACE : edmEntity.namespace();
									String name = edmEntity.name().isEmpty() ? t.getSimpleName() : edmEntity.name();
									parameterType = getFullQualifiedName(namespace, name);
								}
							}
						}
					} else {
						Class<?> otherClazz = fld.getType();
						EdmEnum edmEnum = otherClazz.getAnnotation(EdmEnum.class);
						if(edmEnum != null) {
							String namespace = edmEnum.namespace().isEmpty() ? NAME_SPACE : edmEnum.namespace();
							String name = edmEnum.name().isEmpty() ? otherClazz.getSimpleName() : edmEnum.name();
							parameterType = getFullQualifiedName(namespace, name);
						}
						EdmEntity edmEntity = otherClazz.getAnnotation(EdmEntity.class);
						if(edmEntity != null) {
							String namespace = edmEntity.namespace().isEmpty() ? NAME_SPACE : edmEntity.namespace();
							String name = edmEntity.name().isEmpty() ? otherClazz.getSimpleName() : edmEntity.name();
							parameterType = getFullQualifiedName(namespace, name);
						}
					}
				} else {
					switch(parameter.type()) {
					case "Edm.Int32":
						parameterType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
						break;
					case "Edm.String":
						parameterType = EdmPrimitiveTypeKind.String.getFullQualifiedName();
						break;
					case "Edm.Date":
						parameterType = EdmPrimitiveTypeKind.Date.getFullQualifiedName();
						break;
					case "Edm.Decimal":
						parameterType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
						break;
					}
				}
				
				CsdlParameter csdlParameter = new CsdlParameter()
					.setName(parameterName)
					.setType(parameterType)
					.setNullable(parameter.nullable())
					.setCollection(fieldIsCollection);
				
				action.getParameters().add(csdlParameter);
			}
		}
		
		return action;
	}
	
	@Override
	public List<CsdlFunction> getFunctions(FullQualifiedName functionName) throws ODataException {
		CsdlFunction csdlFunction = getFunction(functionName);
		return csdlFunction == null ? null : Arrays.asList(csdlFunction);
	}

	public CsdlFunction getFunction(FullQualifiedName functionName) throws ODataException {

		String functionNameString = functionName.getName();
		Class<?> clazz = functionsMap.get(functionNameString);
		
		if(clazz == null) return null;
		
		EdmFunction edmFunction = clazz.getAnnotation(EdmFunction.class);
		EdmReturnType edmReturnType = clazz.getAnnotation(EdmReturnType.class);

		boolean isCollection = edmReturnType.type().startsWith("Collection");
		
		CsdlFunction function = new CsdlFunction()
			.setBound(edmFunction.isBound())
			.setName(edmFunction.name());
		if(edmFunction.isBound()) function.setEntitySetPath(edmFunction.entitySetPath());

		if(edmReturnType != null) {
			
			CsdlReturnType returnType = new CsdlReturnType()
				.setCollection(isCollection)
				.setNullable(edmReturnType.nullable());
			
			if(isCollection) {
				returnType.setType(String.format("Collection(%s)", getFullQualifiedName(edmReturnType.type()).toString() ));
			} else {
				returnType.setType(getFullQualifiedName(edmReturnType.type()));
			}
			
			function.setReturnType(returnType);
		}

		if(edmFunction.isBound()) {
			
			Class<?> entitySetClass = entitySetsMap.get(edmFunction.entitySetPath());
			EdmEntity edmEntity = entitySetClass.getAnnotation(EdmEntity.class);
						
			CsdlParameter csdlParameter = new CsdlParameter()
				.setName(edmFunction.entitySetPath())
				.setType(getFullQualifiedName(edmEntity.namespace(), edmEntity.name()));
			
			function.getParameters().add(csdlParameter);
		}
		
		for(Field fld : clazz.getDeclaredFields()) {
			
			EdmParameter parameter = fld.getAnnotation(EdmParameter.class);
			if(parameter != null) {
				
				String parameterName = parameter.name().isEmpty() ? fld.getName() : parameter.name();
				FullQualifiedName parameterType = null;

				if(parameter.type().isEmpty()) {					
					if(fld.getType().isAssignableFrom(Integer.class)) {
						parameterType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(String.class)) {
						parameterType = EdmPrimitiveTypeKind.String.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(LocalDate.class)) {
						parameterType = EdmPrimitiveTypeKind.Date.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(Boolean.class)) {
						parameterType = EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
					} else if(fld.getType().isAssignableFrom(BigDecimal.class)) {
						parameterType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
					} else {
						Class<?> enumClazz = fld.getType();
						EdmEnum edmEnum = enumClazz.getAnnotation(EdmEnum.class);
						if(edmEnum != null) {
							String namespace = edmEnum.namespace().isEmpty() ? NAME_SPACE : edmEnum.namespace();
							String name = edmEnum.name().isEmpty() ? enumClazz.getSimpleName() : edmEnum.name();
							parameterType = getFullQualifiedName(namespace, name);
						}
					}
				} else {
					switch(parameter.type()) {
					case "Edm.Int32":
						parameterType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
						break;
					case "Edm.String":
						parameterType = EdmPrimitiveTypeKind.String.getFullQualifiedName();
						break;
					case "Edm.Date":
						parameterType = EdmPrimitiveTypeKind.Date.getFullQualifiedName();
						break;
					case "Edm.Decimal":
						parameterType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
						break;
					}
				}
				
				CsdlParameter csdlParameter = new CsdlParameter()
					.setName(parameterName)
					.setType(parameterType)
					.setNullable(parameter.nullable());
				
				function.getParameters().add(csdlParameter);
			}
		}
		
		return function;
	}
	
	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		
		LOG.debug("CREATING ENTITY TYPE: {}", entityTypeName);
		
		String entityTypeNameString = entityTypeName.getName();
		String entitySetName = entityTypesMap.get(entityTypeNameString);
		
		Class<?> clazz = entitySetsMap.get(entitySetName);
		if(clazz == null) return null;
		
		EdmEntity edmEntity = clazz.getAnnotation(EdmEntity.class);

		Field[] fields = clazz.getDeclaredFields();
		
		List<CsdlProperty> csdlProperties = getCsdlProperties(fields);
		List<CsdlNavigationProperty> csdlNavigationProperties = getCsdlNavigationProperties(fields);
		List<CsdlPropertyRef> csdlPropertyRefs = Arrays.asList(edmEntity.key()).stream().map(key -> new CsdlPropertyRef().setName(key)).collect(Collectors.toList());
		
		// Validate: Each property ref should match a property
		
		for(CsdlPropertyRef csdlPropertyRef : csdlPropertyRefs) {
			final String refName = csdlPropertyRef.getName();
			final boolean match = csdlProperties.stream().anyMatch(e -> {
				return e.getName().equals(refName);
			});
			if(!match) {
				throw new ODataException(String.format("THE PROPERTY REF %s [%s] DOES NOT MATCH ANY OF THE ENTITY PROPERTIES", refName, entityTypeName));
			}
		}
		
		CsdlEntityType entityType = new CsdlEntityType()
			.setName(edmEntity.name())
    		.setProperties(csdlProperties)
    		.setKey(csdlPropertyRefs)
    		.setNavigationProperties(csdlNavigationProperties)
    		.setHasStream(edmEntity.hasStream());

		return entityType;
	}

	public String getNameSpace() {
		return NAME_SPACE;
	}

	public EdmProvider setNameSpace(String NameSpace) {
		this.NAME_SPACE = NameSpace;
		return this;
	}

	public String getContainerName() {
		return CONTAINER_NAME;
	}

	public EdmProvider setContainerName(String containerName) {
		CONTAINER_NAME = containerName;
		return this;
	}

	public String getServiceRoot() {
		return SERVICE_ROOT;
	}

	public EdmProvider setServiceRoot(String ServiceRoot) {
		SERVICE_ROOT = ServiceRoot;
		return this;
	}

	public String getDefaultEdmPackage() {
		return DEFAULT_EDM_PACKAGE;
	}

	public EdmProvider setDefaultEdmPackage(String DefaultEdmPackage) {
		DEFAULT_EDM_PACKAGE = DefaultEdmPackage;
		return this;
	}

	public HashMap<String, Class<?>> getEntitySetsMap() {
		return entitySetsMap;
	}

	public HashMap<String, Class<?>> getEnumsMap() {
		return enumsMap;
	}

	public HashMap<String, Class<?>> getActionsMap() {
		return actionsMap;
	}

	public HashMap<String, Class<?>> getActionImportsMap() {
		return actionImportsMap;
	}

	public HashMap<String, Class<?>> getFunctionsMap() {
		return functionsMap;
	}

	public HashMap<String, Class<?>> getFunctionImportsMap() {
		return functionImportsMap;
	}

	public HashMap<String, String> getEntityTypesMap() {
		return entityTypesMap;
	}

	private FullQualifiedName getFullQualifiedName(String namespace, String name) {
		if(name.startsWith("Collection")) {
			name = name.subSequence(name.indexOf('(') + 1, name.length() - 1).toString();
		}
		return new FullQualifiedName(namespace, name);
	}
	
	private FullQualifiedName getFullQualifiedName(String name) {
		return getFullQualifiedName(NAME_SPACE, name);
	}
	
	private ClassPathScanningCandidateComponentProvider createComponentScanner(Iterable<Class<? extends Annotation>> annotationTypes) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		for(Class<? extends Annotation> annotationType : annotationTypes) provider.addIncludeFilter(new AnnotationTypeFilter(annotationType));
		return provider;
    }
}
