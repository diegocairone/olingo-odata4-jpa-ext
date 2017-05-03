package com.cairone.olingo.ext.jpa.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAProperty;
import com.cairone.olingo.ext.jpa.visitors.FilterExpressionVisitor;

public final class JPQLQueryBuilder {

	private boolean distinct = true;
	private Class<?> clazz = null;
	
	private ExpandOption expandOption;
	private FilterOption filterOption; 
	private OrderByOption orderByOption;
	
	private Map<String, Object> queryParams = new HashMap<String, Object>();
	
	public JPQLQuery build() throws ExpressionVisitException, ODataApplicationException {
		
		ODataJPAEntity oDataJPAEntity = clazz.getAnnotation(ODataJPAEntity.class);
		String entityName = oDataJPAEntity.value().isEmpty() ? clazz.getSimpleName() : oDataJPAEntity.value();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT ");
		if(distinct) sb.append("DISTINCT ");
		sb.append("e FROM ");
		sb.append(entityName + " e ");
		
		appendExpandOption(sb);
		appendFilterOption(sb);
		appendOrderByOption(sb);
		
		return new JPQLQuery(sb.toString(), queryParams);
	}
	
	public boolean isDistinct() {
        return distinct;
    }

    public JPQLQueryBuilder setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

	public Class<?> getClazz() {
		return clazz;
	}

	public JPQLQueryBuilder setClazz(Class<?> clazz) {
		this.clazz = clazz;
		return this;
	}

	public ExpandOption getExpandOption() {
		return expandOption;
	}

	public JPQLQueryBuilder setExpandOption(ExpandOption expandOption) {
		this.expandOption = expandOption;
		return this;
	}

	public FilterOption getFilterOption() {
		return filterOption;
	}

	public JPQLQueryBuilder setFilterOption(FilterOption filterOption) {
		this.filterOption = filterOption;
		return this;
	}

	public OrderByOption getOrderByOption() {
		return orderByOption;
	}

	public JPQLQueryBuilder setOrderByOption(OrderByOption orderByOption) {
		this.orderByOption = orderByOption;
		return this;
	}
	
	private String substituteByJpaProperty(final Class<?> clazz, final String fieldName) {
		for(Field field : clazz.getDeclaredFields()) {
    		com.cairone.olingo.ext.jpa.annotations.EdmProperty annEdmProperty = field.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
    		if(annEdmProperty != null && (annEdmProperty.name().equals(fieldName) || field.getName().equals(fieldName))) {
    			ODataJPAProperty oDataJPAProperty = field.getAnnotation(ODataJPAProperty.class);
    			if(oDataJPAProperty != null && !oDataJPAProperty.value().isEmpty()) {
    				return oDataJPAProperty.value();
    			}
    		}
    		com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty annEdmNavigationProperty = field.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty.class);
    		if(annEdmNavigationProperty != null && (annEdmNavigationProperty.name().equals(fieldName) || field.getName().equals(fieldName))) {
    			ODataJPAProperty oDataJPAProperty = field.getAnnotation(ODataJPAProperty.class);
    			if(oDataJPAProperty != null && !oDataJPAProperty.value().isEmpty()) {
    				return oDataJPAProperty.value();
    			}
    		}
    	}
		return fieldName;
	}
	
	private void appendExpandOption(StringBuilder sb) {

    	if(expandOption != null && !expandOption.getExpandItems().isEmpty()) {
    		expandOption.getExpandItems().forEach(expandItem -> {
    			UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
    			if(uriResource instanceof UriResourceNavigation) {
    				EdmNavigationProperty edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
    				String navPropName = edmNavigationProperty.getName();
    				for(Field field : clazz.getDeclaredFields()) {
    					com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty annEdmNavigationProperty = field.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty.class);
    					if(annEdmNavigationProperty != null && (annEdmNavigationProperty.name().equals(navPropName) || field.getName().equals(navPropName))) {
    		    			ODataJPAProperty oDataJPAProperty = field.getAnnotation(ODataJPAProperty.class);
    		    			if(oDataJPAProperty != null && !oDataJPAProperty.value().isEmpty()) {
    		    				sb.append("LEFT JOIN FETCH e." + oDataJPAProperty.value() + " ");
    		    			} else {
    		    				sb.append("LEFT JOIN FETCH e." + navPropName + " ");
    		    			}
    		    			break;
    		    		}
    				}
    			}
    		});
    	}
		
	}
	
	private void appendFilterOption(StringBuilder sb) throws ExpressionVisitException, ODataApplicationException {
		
		if(filterOption != null) {
			
			Expression filterExpression = filterOption.getExpression();
			FilterExpressionVisitor filterExpressionVisitor = new FilterExpressionVisitor(clazz, queryParams);
			
			Object visitorResult = filterExpression.accept(filterExpressionVisitor);
			if(visitorResult != null && visitorResult instanceof String) {
				String whereClause = visitorResult.toString();
				if(!whereClause.isEmpty()) sb.append("WHERE " + whereClause);
			}
		}
	}
	
	private void appendOrderByOption(StringBuilder sb) {

		if(orderByOption != null) {
			sb.append("ORDER BY ");
			String orderby = orderByOption.getOrders()
				.stream()
				.map(x -> {
					Expression expression = x.getExpression();
					if(expression instanceof Member){
						
						UriInfoResource resourcePath = ((Member)expression).getResourcePath();
						List<String> segments = new ArrayList<String>();
						segments.add("e");
						
						UriResource uriResourceLast = resourcePath.getUriResourceParts().get(resourcePath.getUriResourceParts().size() - 1);
						
						for(UriResource uriResource : resourcePath.getUriResourceParts()) {
							if(!uriResource.equals(uriResourceLast)) {
								if (uriResource instanceof UriResourcePrimitiveProperty) {
							    	EdmProperty edmProperty = ((UriResourcePrimitiveProperty)uriResource).getProperty();
							    	String fieldName = substituteByJpaProperty(clazz, edmProperty.getName());
							    	segments.add(fieldName);
							    }
								if (uriResource instanceof UriResourceNavigation) {
									EdmNavigationProperty edmProperty = ((UriResourceNavigation)uriResource).getProperty();
									String fieldName = substituteByJpaProperty(clazz, edmProperty.getName());
							    	segments.add(fieldName);
								}
							}
						}
						
						if (uriResourceLast instanceof UriResourcePrimitiveProperty) {
					    	EdmProperty edmProperty = ((UriResourcePrimitiveProperty)uriResourceLast).getProperty();
					    	String fieldName = substituteByJpaProperty(clazz, edmProperty.getName());
					    	segments.add(String.format("%s %s", fieldName, x.isDescending() ? "DESC" : "ASC"));
					    }
						if (uriResourceLast instanceof UriResourceNavigation) {
							EdmNavigationProperty edmProperty = ((UriResourceNavigation)uriResourceLast).getProperty();
							String fieldName = substituteByJpaProperty(clazz, edmProperty.getName());
							segments.add(String.format("%s %s", fieldName, x.isDescending() ? "DESC" : "ASC"));
						}
						
						return segments.stream().collect(Collectors.joining("."));
					}
					return null;
				})
				.collect(Collectors.joining(", "));
			sb.append(orderby);
		}		
	}
}
