package com.cairone.olingo.ext.jpa.visitors;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslEntity;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslProperty;
import com.cairone.olingo.ext.jpa.utilities.Util;
import com.google.common.base.Splitter;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;

public class QueryDslExpressionVisitor extends BaseExpressionVisitor {

	protected static final Logger LOG = LoggerFactory.getLogger(QueryDslExpressionVisitor.class);
	
	private Class<?> targetEdm = null;
	private Class<?> pointerEdmClazz = null;
	private Class<?> pointerJpaClazz = null;
	
	public QueryDslExpressionVisitor() {}
	
	public QueryDslExpressionVisitor(Class<?> targetEdm) {
		super();
		this.targetEdm = targetEdm;
	}

	public QueryDslExpressionVisitor setTargetEntity(Class<?> targetEdm) {
		this.targetEdm = targetEdm;
		return this;
	}

	@Override
	public Expression<?> visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
		
		UriInfoResource uriInfo = member.getResourcePath();
		ODataQueryDslEntity oDataQueryDslEntity = targetEdm.getAnnotation(ODataQueryDslEntity.class);
		
		pointerEdmClazz = targetEdm;
		pointerJpaClazz = oDataQueryDslEntity.jpaentity();
		
		PathBuilder<?> pathBuilder = new PathBuilder<>(oDataQueryDslEntity.jpaentity(), oDataQueryDslEntity.variable());
		
		for(UriResource resource : uriInfo.getUriResourceParts()) {
			switch(resource.getKind()) {
			case primitiveProperty: {
					UriResourcePrimitiveProperty primitiveProperty = (UriResourcePrimitiveProperty) resource;
					String propName = primitiveProperty.getProperty().getName();
					Field edmField = getFieldInEDM(pointerEdmClazz, propName);
					ODataQueryDslProperty oDataQueryDslProperty = edmField.getAnnotation(ODataQueryDslProperty.class);
					String jpaFieldPathName = oDataQueryDslProperty == null || oDataQueryDslProperty.value().trim().isEmpty() ? edmField.getName() : oDataQueryDslProperty.value();		// fieldX.fieldY.fieldZ from jpaEntity
					try {
						pointerEdmClazz.getDeclaredField(edmField.getName());
					} catch (NoSuchFieldException e) {
						jpaFieldPathName = oDataQueryDslEntity.extendsFieldName() + "." + jpaFieldPathName;
					} catch (SecurityException e) {
						LOG.error(e.getMessage(), e);
						throw new ExpressionVisitException(e.getMessage(), e);
					}
					if(primitiveProperty.getProperty().getType().getKind().equals(EdmTypeKind.ENUM)) {
						pointerEdmClazz = edmField.getType();
					}
					return getPath(pointerJpaClazz, pathBuilder, jpaFieldPathName);
				}
			case complexProperty: {
					UriResourceComplexProperty complexProperty = (UriResourceComplexProperty) resource;
					String propName = complexProperty.getProperty().getName();
					Field edmField = getFieldInEDM(pointerEdmClazz, propName);
					ODataQueryDslProperty oDataQueryDslProperty = edmField.getAnnotation(ODataQueryDslProperty.class);
					if(!edmField.getDeclaringClass().equals(pointerEdmClazz)) {
						pathBuilder = (PathBuilder<?>) getPath(pointerJpaClazz, pathBuilder, oDataQueryDslEntity.extendsFieldName());
						pointerEdmClazz = edmField.getDeclaringClass();
						oDataQueryDslEntity = pointerEdmClazz.getAnnotation(ODataQueryDslEntity.class);
						if(oDataQueryDslEntity != null) {
							pointerJpaClazz = oDataQueryDslEntity.jpaentity();
						}
					}
					if(oDataQueryDslProperty != null) {
						String jpaFieldPathName = oDataQueryDslProperty.value().trim().isEmpty() ? edmField.getName() : oDataQueryDslProperty.value();
						if(!oDataQueryDslProperty.type().equals(Object.class)) {
							pathBuilder = (PathBuilder<?>) getPath(pointerJpaClazz, pathBuilder, jpaFieldPathName);
							pointerJpaClazz = oDataQueryDslProperty.type();
						} else {
							if(!oDataQueryDslEntity.extendsFieldName().trim().isEmpty()) {
								jpaFieldPathName = oDataQueryDslEntity.extendsFieldName() +"." + jpaFieldPathName;
							}
							pathBuilder = (PathBuilder<?>) getPath(pointerJpaClazz, pathBuilder, jpaFieldPathName);
							pointerJpaClazz = oDataQueryDslEntity.jpaentity();
						}
					}
					pointerEdmClazz = edmField.getType();
				}
				break;
			case navigationProperty: {
				UriResourceNavigation navigation = (UriResourceNavigation) resource;
					String propName = navigation.getProperty().getName();
					Field edmField = getFieldInEDM(pointerEdmClazz, propName);
					ODataQueryDslProperty oDataQueryDslProperty = edmField.getAnnotation(ODataQueryDslProperty.class);
					String jpaFieldPathName = oDataQueryDslProperty == null || oDataQueryDslProperty.value().trim().isEmpty() ? edmField.getName() : oDataQueryDslProperty.value();		// fieldX.fieldY.fieldZ from jpaEntity
					try {
						pointerEdmClazz.getDeclaredField(edmField.getName());
					} catch (NoSuchFieldException e) {
						jpaFieldPathName = oDataQueryDslEntity.extendsFieldName() + "." + jpaFieldPathName;
					} catch (SecurityException e) {
						LOG.error(e.getMessage(), e);
						throw new ExpressionVisitException(e.getMessage(), e);
					}
					pathBuilder = (PathBuilder<?>) getPath(pointerJpaClazz, pathBuilder, jpaFieldPathName);
					pointerEdmClazz = edmField.getType();
					oDataQueryDslEntity = pointerEdmClazz.getAnnotation(ODataQueryDslEntity.class);
					if(oDataQueryDslProperty != null && !oDataQueryDslProperty.type().equals(Object.class)) {
						pointerJpaClazz = oDataQueryDslProperty.type();
					} else {
						pointerJpaClazz = oDataQueryDslEntity.jpaentity();
					}
				}
				break;
			case entitySet:
			case action:
			case count:
			case function:
			case it:
			case lambdaAll:
			case lambdaAny:
			case lambdaVariable:
			case ref:
			case root:
			case singleton:
			case value:
			default:
				break;			
			}
		}
		
		return pathBuilder;
	}

	@Override
	public Expression<?> visitEnum(EdmEnumType type, List<String> enumValues) throws ExpressionVisitException, ODataApplicationException {
		if(pointerEdmClazz.isEnum()) {
			List<Enum<?>> selectedEnums = new ArrayList<>();
			Enum<?>[] enums = (Enum<?>[]) pointerEdmClazz.getEnumConstants();
			for(Enum<?> enumeration : enums) {
				String value = enumeration.name();
				if(enumValues.contains(value)) {
					selectedEnums.add(enumeration);
				}
			}
			if(!selectedEnums.isEmpty()) {
				return Expressions.constant(selectedEnums);
			}
		}
		return null;
	}
	
	private Field getFieldInEDM(final Class<?> edmEntityClazz, final String propertyName) {
		Field[] fields = Util.getFields(edmEntityClazz);
		String formatedPropertyName = propertyName;
		for(Field field : fields) {
			EdmProperty edmProperty = field.getAnnotation(EdmProperty.class);
			EdmNavigationProperty edmNavigationProperty = field.getAnnotation(EdmNavigationProperty.class);
			if(edmProperty != null && edmProperty.name().trim().isEmpty()) {
				formatedPropertyName = Util.revertNamingConvention(edmProperty, formatedPropertyName);
			} else if(edmNavigationProperty != null && edmNavigationProperty.name().trim().isEmpty()) {
				formatedPropertyName = Util.revertNamingConvention(edmNavigationProperty, formatedPropertyName);
			}
			if(edmProperty == null && field.getName().equals(propertyName)) {
				return field;
			} else if(edmProperty != null && !edmProperty.name().isEmpty() && edmProperty.name().equals(formatedPropertyName)) {
				return field;
			} else if(edmProperty != null && edmProperty.name().isEmpty() && field.getName().equals(formatedPropertyName)) {
				return field;
			} else if(edmNavigationProperty == null && field.getName().equals(formatedPropertyName)) {
				return field;
			} else if(edmNavigationProperty != null && !edmNavigationProperty.name().isEmpty() && edmNavigationProperty.name().equals(formatedPropertyName)) {
				return field;
			} else if(edmNavigationProperty != null && edmNavigationProperty.name().isEmpty() && field.getName().equals(formatedPropertyName)) {
				return field;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Path<?> getPath(final Class<?> rootJpaEntity, final Path<?> path, final String jpaFieldPathName) {
		List<String> parts = Splitter.on('.').trimResults().splitToList(jpaFieldPathName);
		Class<?> pointer = rootJpaEntity;
		PathBuilder<?> pathBuilder = (PathBuilder<?>) path;
		
		for(String fieldName : parts) {
			Field[] fields = Util.getFields(pointer);
			for(Field field : fields) {
				if(field.getName().equals(fieldName)) {
					pointer = field.getType();
					if(pointer.isEnum()) {
						return pathBuilder.getEnum(fieldName, Enum.class);
					} else if(pointer.isAssignableFrom(Integer.class)) {
						Class<Integer> integerType = (Class<Integer>) pointer;
						return pathBuilder.getNumber(fieldName, integerType);
					} else if(pointer.isAssignableFrom(Long.class)) {
						Class<Long> longType = (Class<Long>) pointer;
						return pathBuilder.getNumber(fieldName, longType);
					} else if(pointer.isAssignableFrom(LocalDate.class)) {
						Class<LocalDate> localDateType = (Class<LocalDate>) pointer;
						return pathBuilder.getDate(fieldName, localDateType);
					} else if(pointer.isAssignableFrom(LocalDateTime.class)) {
						Class<LocalDateTime> localDateTimeType = (Class<LocalDateTime>) pointer;
						return pathBuilder.getDate(fieldName, localDateTimeType);
					} else if(pointer.isAssignableFrom(Date.class)) {
						Class<Date> dateType = (Class<Date>) pointer;
						return pathBuilder.getDate(fieldName, dateType);
					} else if(pointer.isAssignableFrom(String.class)) {
						return pathBuilder.getString(fieldName);
					} else if(pointer.isAssignableFrom(Boolean.class)) {
						return pathBuilder.getBoolean(fieldName);
					} else if(pointer.isAnnotationPresent(Entity.class)) {
						pathBuilder = pathBuilder.get(fieldName, pointer);
					}
					break;
				}
			}
		}
		
		return pathBuilder;
	}
}
