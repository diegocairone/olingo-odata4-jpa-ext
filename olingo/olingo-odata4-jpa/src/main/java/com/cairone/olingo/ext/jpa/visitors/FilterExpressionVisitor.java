package com.cairone.olingo.ext.jpa.visitors;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAProperty;
import com.cairone.olingo.ext.jpa.converters.BinaryOperatorConverter;
import com.cairone.olingo.ext.jpa.enums.BinaryOperatorGroup;
import com.google.common.base.CharMatcher;
import com.google.common.primitives.Ints;

public class FilterExpressionVisitor implements ExpressionVisitor<Object> {
	
	private static Logger logger = LoggerFactory.getLogger(FilterExpressionVisitor.class);
	
	private Class<?> clazz;
	private Map<String, Object> queryParams = null;
	
	private int paramCount = 0;

	public FilterExpressionVisitor(Class<?> clazz, Map<String, Object> queryParams) {
		super();
		this.clazz = clazz;
		this.queryParams = queryParams;
	}

	@Override
	public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right) throws ExpressionVisitException, ODataApplicationException {
		
		logger.info("METHOD: visitBinaryOperator");
		
		BinaryOperatorGroup binaryOperatorGroup = BinaryOperatorGroup.from(operator);
		BinaryOperatorConverter converter = new BinaryOperatorConverter();
		
		StringBuilder sb = new StringBuilder();
		
		if(binaryOperatorGroup.equals(BinaryOperatorGroup.LOGICAL_OPERATOR)) {
			
			sb.append(left.toString());
			sb.append(converter.convertToJpqlOperator(operator));
			sb.append(right.toString());
			
			return sb.toString();
		}
		
		String param = "value" + (paramCount++);
		
		sb.append(left.toString());
		sb.append(converter.convertToJpqlOperator(operator) + ":");
		sb.append(param);
		
		Integer intParam = Ints.tryParse(right.toString());
		if(intParam != null) {
			queryParams.put(param, intParam);
			return sb.toString();
		}
		
		queryParams.put(param, CharMatcher.is('\'').trimFrom(right.toString()));
		return sb.toString();
	}

	@Override
	public Object visitUnaryOperator(UnaryOperatorKind operator, Object operand) throws ExpressionVisitException, ODataApplicationException {
		logger.info("METHOD: visitUnaryOperator");
		if(operator.equals(UnaryOperatorKind.NOT)) {
			return String.format("NOT (%s)", operand.toString());
		}
		return operand == null ? null : operand.toString();
	}

	@Override
	public Object visitMethodCall(MethodKind methodCall, List<Object> parameters) throws ExpressionVisitException, ODataApplicationException {
		logger.info("METHOD: visitMethodCall");
		
		StringBuilder sb = new StringBuilder();
		
		if(methodCall.equals(MethodKind.CONTAINS) || methodCall.equals(MethodKind.STARTSWITH) || methodCall.equals(MethodKind.ENDSWITH)) {
			if(parameters.get(0) instanceof String && parameters.get(1) instanceof String) {
				
				String propertyName = (String) parameters.get(0);
				if(propertyName.startsWith("e.")) propertyName = propertyName.substring(2, propertyName.length());
				updatePropertyName(propertyName);
				
				String propertyValue = (String) parameters.get(1);
				String param = "value" + (paramCount++);
				
				sb.append("e.").append(propertyName).append(" LIKE :").append(param);
				
				if(methodCall.equals(MethodKind.CONTAINS)) queryParams.put(param, CharMatcher.is('\'').replaceFrom(propertyValue, '%'));
				if(methodCall.equals(MethodKind.STARTSWITH)) queryParams.put(param, CharMatcher.is('\'').trimFrom(propertyValue) + "%");
				if(methodCall.equals(MethodKind.ENDSWITH)) queryParams.put(param, "%" + CharMatcher.is('\'').trimFrom(propertyValue));
				
				return sb.toString();
			} else {
				throw new ODataApplicationException("Contains needs two parametes of type Edm.String", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		} else if(methodCall.equals(MethodKind.DAY) || methodCall.equals(MethodKind.MONTH) || methodCall.equals(MethodKind.YEAR)) {
			if(parameters.get(0) instanceof String) {
				
				String propertyName = (String) parameters.get(0);
				if(propertyName.startsWith("e.")) propertyName = propertyName.substring(2, propertyName.length());
				updatePropertyName(propertyName);
				
				sb.append(methodCall.toString().toUpperCase()).append("(e.").append(propertyName).append(")");

				return sb.toString();
			} else {
				throw new ODataApplicationException("Day needs one parameter of type Edm.String", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		} else {
			throw new ODataApplicationException("Method call " + methodCall + " not implemented",
					HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
		}
	}

	@Override
	public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression) throws ExpressionVisitException, ODataApplicationException {
		logger.info("METHOD: visitLambdaExpression");
		return null;
	}

	@Override
	public Object visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
		logger.info("METHOD: visitLiteral");
		return literal.getText();
	}

	@Override
	public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {

		logger.info("METHOD: visitMember");
		
		UriInfoResource uriInfoResource = member.getResourcePath();
		final List<UriResource> uriResourceParts = uriInfoResource.getUriResourceParts();
		
		for(UriResource uriResource : uriResourceParts) {
			
			if(uriResource instanceof UriResourcePrimitiveProperty) {
				UriResourcePrimitiveProperty uriResourceProperty = (UriResourcePrimitiveProperty) uriResource;
				String propertyName = uriResourceProperty.getProperty().getName();
				for(Field field : clazz.getDeclaredFields()) {
					EdmProperty annEdmProperty = field.getAnnotation(EdmProperty.class);
					if(annEdmProperty != null && (annEdmProperty.name().equals(propertyName) || field.getName().equals(propertyName))) {
						ODataJPAProperty oDataJPAProperty = field.getAnnotation(ODataJPAProperty.class);
						if(oDataJPAProperty != null && !oDataJPAProperty.value().isEmpty()) {
							propertyName = oDataJPAProperty.value();
						}
						return "e." + propertyName;
					}
				}
			}
		}
		
		throw new ODataApplicationException("NO SEGMENTS IN RESOURCE PATH", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
		logger.info("METHOD: visitAlias");
		return null;
	}

	@Override
	public Object visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
		logger.info("METHOD: visitTypeLiteral");
		return null;
	}

	@Override
	public Object visitLambdaReference(String variableName) throws ExpressionVisitException, ODataApplicationException {
		logger.info("METHOD: visitLambdaReference");
		return null;
	}

	@Override
	public Object visitEnum(EdmEnumType type, List<String> enumValues) throws ExpressionVisitException, ODataApplicationException {
		logger.info("METHOD: visitEnum");
		return null;
	}
	
	private void updatePropertyName(String propertyName) throws ODataApplicationException {

		EdmProperty edmProperty = null;
		ODataJPAProperty oDataJPAProperty = null;
		
		for(Field field : clazz.getDeclaredFields()) {
			EdmProperty annEdmProperty = field.getAnnotation(EdmProperty.class);
			if(annEdmProperty != null && (annEdmProperty.name().equals(propertyName) || field.getName().equals(propertyName))) {
				edmProperty = annEdmProperty;
				oDataJPAProperty = field.getAnnotation(ODataJPAProperty.class);
				break;
			}
		}

		if(edmProperty == null) {
			throw new ODataApplicationException("Property not found on entity", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		} else {
			if(oDataJPAProperty != null && !oDataJPAProperty.value().isEmpty()) {
				propertyName = oDataJPAProperty.value();
			}
		}
	}
}
