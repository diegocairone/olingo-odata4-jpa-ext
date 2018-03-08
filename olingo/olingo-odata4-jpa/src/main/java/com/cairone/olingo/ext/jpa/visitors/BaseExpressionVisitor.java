package com.cairone.olingo.ext.jpa.visitors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

import com.google.common.base.CharMatcher;
import com.mysema.query.support.Expressions;
import com.mysema.query.types.Constant;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.BooleanOperation;
import com.mysema.query.types.expr.SimpleExpression;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.EnumPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public abstract class BaseExpressionVisitor implements ExpressionVisitor<Expression<?>> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Expression<?> visitBinaryOperator(BinaryOperatorKind operator, Expression<?> left, Expression<?> right) throws ExpressionVisitException, ODataApplicationException {

		if(right == null && left != null && left instanceof Path<?>) {
			SimpleExpression<?> expression = (SimpleExpression<?>) left;
			return operator.equals(BinaryOperatorKind.EQ) ? expression.isNull() : operator.equals(BinaryOperatorKind.NE) ? expression.isNotNull() : null;
		}
		
		switch(operator) {
		case HAS:
			if(right instanceof Constant<?>) {
				Constant<?> constant = (Constant<?>) right;
				if(Collection.class.isAssignableFrom(constant.getType())) {
					List<Enum<?>> enumList = (List<Enum<?>>) constant.getConstant();
					if(left instanceof EnumPath<?>) {
						EnumPath path = (EnumPath) left;
						Predicate filter = path.in(enumList);
						return filter;
					}
				}
			}
			break;
		case EQ:
		case NE:
		case GT:
		case GE:
		case LT:
		case LE:
			if(right instanceof Constant<?>) {
				Constant<?> constant = (Constant<?>) right;
				if(constant.getType().isAssignableFrom(Integer.class)) {
					NumberPath<Integer> path = (NumberPath<Integer>) left;
					Integer value = right == null ? null : (Integer) constant.getConstant();				
					Predicate filter = operator.equals(BinaryOperatorKind.EQ) ? path.eq(value)
							: operator.equals(BinaryOperatorKind.NE) ? path.ne(value) 
							: operator.equals(BinaryOperatorKind.GT) ? path.gt(value)
							: operator.equals(BinaryOperatorKind.GE) ? path.goe(value) 
							: operator.equals(BinaryOperatorKind.LT) ? path.lt(value) 
							: operator.equals(BinaryOperatorKind.LE) ? path.loe(value) : null;
					return filter;
				} else if (constant.getType().isAssignableFrom(Long.class)) {
					NumberPath<Long> path = (NumberPath<Long>) left;
					Long value = right == null ? null : (Long) constant.getConstant();
					Predicate filter = operator.equals(BinaryOperatorKind.EQ) ? path.eq(value)
							: operator.equals(BinaryOperatorKind.NE) ? path.ne(value) 
							: operator.equals(BinaryOperatorKind.GT) ? path.gt(value)
							: operator.equals(BinaryOperatorKind.GE) ? path.goe(value) 
							: operator.equals(BinaryOperatorKind.LT) ? path.lt(value) 
							: operator.equals(BinaryOperatorKind.LE) ? path.loe(value) : null;
					return filter;
				} else if (constant.getType().isAssignableFrom(LocalDate.class)) {
					DatePath<LocalDate> path = (DatePath<LocalDate>) left;
					LocalDate value = right == null ? null : (LocalDate) constant.getConstant();
					Predicate filter = operator.equals(BinaryOperatorKind.EQ) ? path.eq(value)
							: operator.equals(BinaryOperatorKind.NE) ? path.ne(value) 
							: operator.equals(BinaryOperatorKind.GT) ? path.gt(value)
							: operator.equals(BinaryOperatorKind.GE) ? path.goe(value) 
							: operator.equals(BinaryOperatorKind.LT) ? path.lt(value) 
							: operator.equals(BinaryOperatorKind.LE) ? path.loe(value) : null;
					return filter;
				} else if (constant.getType().isAssignableFrom(LocalDateTime.class)) {
					DatePath<LocalDateTime> path = (DatePath<LocalDateTime>) left;
					LocalDateTime value = right == null ? null : (LocalDateTime) constant.getConstant();
					Predicate filter = operator.equals(BinaryOperatorKind.EQ) ? path.eq(value)
							: operator.equals(BinaryOperatorKind.NE) ? path.ne(value) 
							: operator.equals(BinaryOperatorKind.GT) ? path.gt(value)
							: operator.equals(BinaryOperatorKind.GE) ? path.goe(value) 
							: operator.equals(BinaryOperatorKind.LT) ? path.lt(value) 
							: operator.equals(BinaryOperatorKind.LE) ? path.loe(value) : null;
					return filter;
				} else {
					StringPath path = (StringPath) left;
					String value = right == null ? null : (String) constant.getConstant();
					Predicate filter = operator.equals(BinaryOperatorKind.EQ) ? path.eq(value)
							: operator.equals(BinaryOperatorKind.NE) ? path.ne(value) 
							: operator.equals(BinaryOperatorKind.GT) ? path.gt(value)
							: operator.equals(BinaryOperatorKind.GE) ? path.goe(value) 
							: operator.equals(BinaryOperatorKind.LT) ? path.lt(value) 
							: operator.equals(BinaryOperatorKind.LE) ? path.loe(value) : null;
					return filter;
				}
			}
		case AND: {
				BooleanExpression leftExp = (BooleanExpression) left;
				BooleanExpression rightExp = (BooleanExpression) right;
				BooleanExpression exp = leftExp.and(rightExp);
				return exp;
			}
		case OR: {
				BooleanExpression leftExp = (BooleanExpression) left;
				BooleanExpression rightExp = (BooleanExpression) right;
				BooleanExpression exp = leftExp.or(rightExp);
				return exp;
			}
		default:
			break;
		}

		return null;
	}

	@Override
	public Expression<?> visitUnaryOperator(UnaryOperatorKind operator, Expression<?> operand) throws ExpressionVisitException, ODataApplicationException {
		if(operator.equals(UnaryOperatorKind.NOT)) {
			BooleanOperation booleanOperation = (BooleanOperation) operand;
			return booleanOperation.not();
		}
		return null;
	}

	@Override
	public Expression<?> visitMethodCall(MethodKind methodCall, List<Expression<?>> parameters) throws ExpressionVisitException, ODataApplicationException {
		StringPath path = (StringPath) parameters.get(0);
		
		@SuppressWarnings("unchecked")
		Expression<String> value = (Expression<String>) parameters.get(1);
		
		if(methodCall.equals(MethodKind.STARTSWITH)) {
			Predicate filter = path.startsWith(value);
			return filter;
		} else if(methodCall.equals(MethodKind.ENDSWITH)) {
			Predicate filter = path.endsWith(value);
			return filter;
		} else if(methodCall.equals(MethodKind.CONTAINS)) {
			Predicate filter = path.contains(value);
			return filter;
		}
		return null;
	}

	@Override
	public Expression<?> visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {

		String text = literal.getText();
		EdmType type = literal.getType();
		
		if(type == null) return null;
		
		switch(type.getName()) {
		case "Int16":
		case "Int32":
		case "SByte":
			Integer integerValue = Integer.valueOf(text);
			return Expressions.constant(integerValue);
		case "Int64":
			Long longValue = Long.valueOf(text);
			return Expressions.constant(longValue);
		case "Date":
			LocalDate localDateValue = LocalDate.parse(text);
			return Expressions.constant(localDateValue);
		case "DateTimeOffset":
			LocalDateTime localDateTimeValue = LocalDateTime.parse(text);
			return Expressions.constant(localDateTimeValue);
		default:
			return Expressions.constant(CharMatcher.is('\'').trimFrom(text));
		}
	}

	@Override
	public Expression<?> visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
		throw new ExpressionVisitException("visitAlias NOT IMPLEMENTED");
	}

	@Override
	public Expression<?> visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
		throw new ExpressionVisitException("visitTypeLiteral NOT IMPLEMENTED");
	}

	@Override
	public Expression<?> visitLambdaReference(String variableName) throws ExpressionVisitException, ODataApplicationException {
		throw new ExpressionVisitException("visitLambdaReference NOT IMPLEMENTED");
	}

	@Override
	public Expression<?> visitLambdaExpression(String lambdaFunction, String lambdaVariable, org.apache.olingo.server.api.uri.queryoption.expression.Expression expression) throws ExpressionVisitException, ODataApplicationException {
		throw new ExpressionVisitException("visitLambdaExpression NOT IMPLEMENTED");
	}

	@Override
	public abstract Expression<?> visitMember(Member member) throws ExpressionVisitException, ODataApplicationException;

	@Override
	public abstract Expression<?> visitEnum(EdmEnumType type, List<String> enumValues) throws ExpressionVisitException, ODataApplicationException;
}
