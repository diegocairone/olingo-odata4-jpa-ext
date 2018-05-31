package com.cairone.olingo.ext.jpa.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAProperty;
import com.cairone.olingo.ext.jpa.visitors.FilterExpressionVisitor;
import com.google.common.base.Splitter;

@Deprecated
public final class JPQLQueryBuilder {
	
	protected static final Logger LOG = LoggerFactory.getLogger(JPQLQueryBuilder.class);

	private boolean distinct = true;
	private Class<?> clazz = null;
	
	private ExpandOption expandOption;
	private FilterOption filterOption; 
	private OrderByOption orderByOption;
	
	private Map<String, Object> queryParams = new HashMap<String, Object>();
	
	public JPQLQuery build() throws ODataApplicationException {

		ODataJPAEntity oDataJPAEntity = clazz.getAnnotation(ODataJPAEntity.class);
		String entityName = oDataJPAEntity == null ? clazz.getSimpleName() : oDataJPAEntity.value() == null || oDataJPAEntity.value().trim().isEmpty() ? oDataJPAEntity.entity().getSimpleName() : oDataJPAEntity.value();
		
		return build(entityName);
	}
	
	public JPQLQuery build(String entityName) throws ODataApplicationException {
		
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
	
	private String substituteByJpaProperty(final Class<?> clazz, final String propertyName) {
		for(Field field : clazz.getDeclaredFields()) {
    		com.cairone.olingo.ext.jpa.annotations.EdmProperty annEdmProperty = field.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmProperty.class);
    		if(annEdmProperty != null && (annEdmProperty.name().equals(propertyName) || field.getName().equals(propertyName))) {
    			ODataJPAProperty oDataJPAProperty = field.getAnnotation(ODataJPAProperty.class);
    			if(oDataJPAProperty != null && !oDataJPAProperty.value().isEmpty() && !oDataJPAProperty.ignore()) {
    				return oDataJPAProperty.value();
    			} else if(oDataJPAProperty != null && oDataJPAProperty.ignore()) {
    				return null;
    			} else {
    				return field.getName();
    			}
    		}
    		com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty annEdmNavigationProperty = field.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty.class);
    		if(annEdmNavigationProperty != null && (annEdmNavigationProperty.name().equals(propertyName) || field.getName().equals(propertyName))) {
    			ODataJPAProperty oDataJPAProperty = field.getAnnotation(ODataJPAProperty.class);
    			if(oDataJPAProperty != null && !oDataJPAProperty.value().isEmpty() && !oDataJPAProperty.ignore()) {
    				return oDataJPAProperty.value();
    			} else if(oDataJPAProperty != null && oDataJPAProperty.ignore()) {
    				return null;
    			} else {
    				return field.getName();
    			}
    		}
    	}
		return propertyName;
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private void appendExpandOptionDeprecated(StringBuilder sb) {

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
    		    			if(oDataJPAProperty != null && !oDataJPAProperty.value().isEmpty() && !oDataJPAProperty.ignore()) {
    		    				sb.append("LEFT JOIN FETCH e." + oDataJPAProperty.value() + " ");
    		    			} else if(oDataJPAProperty == null || !oDataJPAProperty.ignore()) {
    		    				sb.append("LEFT JOIN FETCH e." + field.getName() + " ");
    		    			}
    		    			break;
    		    		}
    				}
    			}
    		});
    	}
		
	}
	
	private void appendExpandOption(StringBuilder sb) throws ODataApplicationException {
		
		if(expandOption != null && !expandOption.getExpandItems().isEmpty()) {
			
			List<ExpandItem> expandItems = expandOption.getExpandItems();
			int count = 0;
			
			for(ExpandItem expandItem : expandItems) {
				UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
    			if(uriResource instanceof UriResourceNavigation) {
    				EdmNavigationProperty edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
    				String navPropName = edmNavigationProperty.getName();
    				for(Field field : clazz.getDeclaredFields()) {
    					com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty annEdmNavigationProperty = field.getAnnotation(com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty.class);
    					if(annEdmNavigationProperty != null && (annEdmNavigationProperty.name().equals(navPropName) || field.getName().equals(navPropName))) {
    						ODataJPAProperty oDataJPAProperty = field.getAnnotation(ODataJPAProperty.class);
    						if(oDataJPAProperty != null && !oDataJPAProperty.value().isEmpty() && !oDataJPAProperty.ignore()) {
    		    				
    							String value = oDataJPAProperty.value();
    							String regex = "^[a-z.][a-zA-Z.0-9]*$";
    							
    							if(!value.matches(regex)) {
    								throw new ODataApplicationException(
    										String.format("VALUE FOR ODataJPAProperty IN FIELD %s [%s] IS NOT VALID", field.getName(), clazz.getName()), 
    										HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
    										Locale.ENGLISH);
    							}
    							
    							Iterable<String> result = Splitter.on('.')
    								       .omitEmptyStrings()
    								       .split(value);
    							
    							LOG.debug("JPA PROPERTY TO EVALUATE: {}", value);
    							String joinFetchExpr = "";
    							boolean isFirst = true;
    							
    							for(String fieldName : result) {
    								count++;
    								if(isFirst) {
    									isFirst = false;
    									joinFetchExpr += "LEFT JOIN FETCH e." + fieldName + " e" + count + " ";
    								} else {
    									joinFetchExpr += "LEFT JOIN FETCH e" + (count - 1) + "." + fieldName + " e" + count + " ";
    								}
    							}
    							
    							LOG.debug("EXPRESSION TO APPEND: {}", joinFetchExpr);
    							sb.append(joinFetchExpr);
    							
    		    			} else if(oDataJPAProperty == null || !oDataJPAProperty.ignore()) {
    		    				sb.append("LEFT JOIN FETCH e." + field.getName() + " ");
    		    			}
    					}
    				}
    			}
			}
		}
	}
	
	private void appendFilterOption(StringBuilder sb) throws ODataApplicationException {
		
		if(filterOption != null) {
			
			Expression filterExpression = filterOption.getExpression();
			FilterExpressionVisitor filterExpressionVisitor = new FilterExpressionVisitor(clazz, queryParams);
			
			try {
				Object visitorResult = filterExpression.accept(filterExpressionVisitor);
				if(visitorResult != null && visitorResult instanceof String) {
					String whereClause = visitorResult.toString();
					if(!whereClause.isEmpty()) sb.append("WHERE " + whereClause);
				}
			} catch (ExpressionVisitException e) {
				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
		}
	}
	
	private void appendOrderByOption(StringBuilder sb) {

		if(orderByOption != null) {
			sb.append(" ORDER BY ");
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
							    	if(fieldName != null) segments.add(fieldName);
							    }
								if (uriResource instanceof UriResourceNavigation) {
									EdmNavigationProperty edmProperty = ((UriResourceNavigation)uriResource).getProperty();
									String fieldName = substituteByJpaProperty(clazz, edmProperty.getName());
									if(fieldName != null) segments.add(fieldName);
								}
							}
						}
						
						if (uriResourceLast instanceof UriResourcePrimitiveProperty) {
					    	EdmProperty edmProperty = ((UriResourcePrimitiveProperty)uriResourceLast).getProperty();
					    	String fieldName = substituteByJpaProperty(clazz, edmProperty.getName());
					    	if(fieldName != null) segments.add(String.format("%s %s", fieldName, x.isDescending() ? "DESC" : "ASC"));
					    }
						if (uriResourceLast instanceof UriResourceNavigation) {
							EdmNavigationProperty edmProperty = ((UriResourceNavigation)uriResourceLast).getProperty();
							String fieldName = substituteByJpaProperty(clazz, edmProperty.getName());
							if(fieldName != null) segments.add(String.format("%s %s", fieldName, x.isDescending() ? "DESC" : "ASC"));
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
