package com.cairone.olingo.ext.jpa.visitors;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslEntity;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslProperty;
import com.cairone.olingo.ext.jpa.utilities.Util;
import com.mysema.query.support.Expressions;
import com.mysema.query.types.Expression;
import com.mysema.query.types.path.PathBuilder;

public class QueryDslExpressionVisitor extends BaseExpressionVisitor {

	private Class<?> targetEdm = null;
	private Class<?> pointerClazz = null;
	
	public QueryDslExpressionVisitor() {}
	
	public QueryDslExpressionVisitor(Class<?> targetEdm) {
		super();
		this.targetEdm = targetEdm;
	}

	public QueryDslExpressionVisitor setTargetEntity(Class<?> targetEdm) {
		this.targetEdm = targetEdm;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Expression<?> visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
		
		UriInfoResource uriInfo = member.getResourcePath();
		
		pointerClazz = targetEdm;
		ODataQueryDslEntity oDataQueryDslEntity = pointerClazz.getAnnotation(ODataQueryDslEntity.class);
		
		PathBuilder<?> pathBuilder = new PathBuilder<>(oDataQueryDslEntity.jpaentity(), oDataQueryDslEntity.variable());
		
		for(UriResource resource : uriInfo.getUriResourceParts()) {
			switch(resource.getKind()) {
			case primitiveProperty: {
					UriResourcePrimitiveProperty primitiveProperty = (UriResourcePrimitiveProperty) resource;
					String propName = primitiveProperty.getProperty().getName();
					Field field = getField(pointerClazz, propName);
					ODataQueryDslProperty oDataQueryDslProperty = field.getAnnotation(ODataQueryDslProperty.class);
					Class<?> type = oDataQueryDslProperty == null || oDataQueryDslProperty.jpaproperty().equals(Object.class) ? field.getType() : oDataQueryDslProperty.jpaproperty();
					String name = oDataQueryDslProperty == null ? field.getName() : oDataQueryDslProperty.value();
					if(type.isEnum()) {
						pointerClazz = field.getType();
						return pathBuilder.getEnum(name, Enum.class);
					} else if(type.isAssignableFrom(Integer.class)) {
						Class<Integer> integerType = (Class<Integer>) type;
						return pathBuilder.getNumber(name, integerType);
					} else if(type.isAssignableFrom(Long.class)) {
						Class<Long> longType = (Class<Long>) type;
						return pathBuilder.getNumber(name, longType);
					} else if(type.isAssignableFrom(LocalDate.class)) {
						Class<LocalDate> localDateType = (Class<LocalDate>) type;
						return pathBuilder.getDate(name, localDateType);
					} else if(type.isAssignableFrom(LocalDateTime.class)) {
						Class<LocalDateTime> localDateTimeType = (Class<LocalDateTime>) type;
						return pathBuilder.getDate(name, localDateTimeType);
					} else if(type.isAssignableFrom(Date.class)) {
						Class<Date> dateType = (Class<Date>) type;
						return pathBuilder.getDate(name, dateType);
					} else if(type.isAssignableFrom(String.class)) {
						return pathBuilder.getString(name);
					}
				}
				break;
			case complexProperty: {
					UriResourceComplexProperty complexProperty = (UriResourceComplexProperty) resource;
					String propName = complexProperty.getProperty().getName();
					Field field = getField(pointerClazz, propName);
					ODataQueryDslProperty oDataQueryDslProperty = field.getAnnotation(ODataQueryDslProperty.class);
					if(oDataQueryDslProperty != null) {
						Class<?> type = oDataQueryDslProperty.jpaproperty();
						String name = oDataQueryDslProperty == null ? field.getName() : oDataQueryDslProperty.value();
						pathBuilder = pathBuilder.get(name, type);
					}
					pointerClazz = field.getType();
				}
				break;
			case navigationProperty: {
				UriResourceNavigation navigation = (UriResourceNavigation) resource;
					String propName = navigation.getProperty().getName();
					Field field = getField(pointerClazz, propName);
					ODataQueryDslProperty oDataQueryDslProperty = field.getAnnotation(ODataQueryDslProperty.class);
					Class<?> type = null;
					if(oDataQueryDslProperty == null) {
						Class<?> fieldClass = field.getType();
						ODataQueryDslEntity fieldTypeAnnotation = fieldClass.getAnnotation(ODataQueryDslEntity.class);
						type = fieldTypeAnnotation.jpaentity();
					} else {
						type = oDataQueryDslProperty.jpaproperty();
					}
					String name = oDataQueryDslProperty == null ? field.getName() : oDataQueryDslProperty.value();
					pathBuilder = pathBuilder.get(name, type);
					pointerClazz = field.getType();
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
		if(pointerClazz.isEnum()) {
			List<Enum<?>> selectedEnums = new ArrayList<>();
			Enum<?>[] enums = (Enum<?>[]) pointerClazz.getEnumConstants();
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
	
	private Field getField(Class<?> clazz, String propertyName) {
		Field[] fields = Util.getFields(clazz);
		for(Field field : fields) {
			EdmProperty edmProperty = field.getAnnotation(EdmProperty.class);
			EdmNavigationProperty edmNavigationProperty = field.getAnnotation(EdmNavigationProperty.class);
			if(edmProperty == null && field.getName().equals(propertyName)) {
				return field;
			} else if(edmProperty != null && !edmProperty.name().isEmpty() && edmProperty.name().equals(propertyName)) {
				return field;
			} else if(edmProperty != null && edmProperty.name().isEmpty() && field.getName().equals(propertyName)) {
				return field;
			} else if(edmNavigationProperty == null && field.getName().equals(propertyName)) {
				return field;
			} else if(edmNavigationProperty != null && !edmNavigationProperty.name().isEmpty() && edmNavigationProperty.name().equals(propertyName)) {
				return field;
			} else if(edmNavigationProperty != null && edmNavigationProperty.name().isEmpty() && field.getName().equals(propertyName)) {
				return field;
			}
		}
		return null;
	}
}
