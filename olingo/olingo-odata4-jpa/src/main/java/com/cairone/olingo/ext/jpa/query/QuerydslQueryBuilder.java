package com.cairone.olingo.ext.jpa.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.cairone.olingo.ext.jpa.interfaces.QueryOptions;
import com.cairone.olingo.ext.jpa.visitors.QueryDslExpressionVisitor;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

public class QuerydslQueryBuilder {

	protected static final Logger LOG = LoggerFactory.getLogger(QuerydslQueryBuilder.class);
	
	private Class<?> clazz = null;
	
	private FilterOption filterOption = null; 
	private OrderByOption orderByOption = null;
	private QueryOptions queryOptions = null;
	
	public QuerydslQuery build() throws ODataApplicationException {
		
		BooleanExpression booleanExpression = getBooleanExpression(); 
		OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers();
		
		QuerydslQuery dslQuery = new QuerydslQuery(booleanExpression, orderSpecifiers);
		
		if(queryOptions != null && (!queryOptions.isSkipByLib() || !queryOptions.isTopByLib())) {
			
			List<Order> orders = orderSpecifiers == null ? null : Arrays.stream(orderSpecifiers)
				.map(orderSpecifier -> {
					com.querydsl.core.types.Expression<?> target = orderSpecifier.getTarget();
					Object targetElement = target instanceof Path ? preparePropertyPath((Path<?>) target) : target;
					return Order.by(targetElement.toString()).with(orderSpecifier.isAscending() ? Direction.ASC : Direction.DESC);
				})
				.collect(Collectors.toList());
			
			PageRequest pageRequest = PageRequest.of(
					queryOptions.isSkipByLib() ? 0 : queryOptions.getSkip(), 
					queryOptions.isTopByLib() ? Integer.MAX_VALUE : queryOptions.getTop(),
					orders == null ? Sort.unsorted() : Sort.by(orders));
			
			dslQuery.setPageable(pageRequest);
		}
		
		return dslQuery;
	}
	
	public QuerydslQueryBuilder setClazz(Class<?> clazz) {
		this.clazz = clazz;
		return this;
	}

	public QuerydslQueryBuilder setFilterOption(FilterOption filterOption) {
		this.filterOption = filterOption;
		return this;
	}

	public QuerydslQueryBuilder setOrderByOption(OrderByOption orderByOption) {
		this.orderByOption = orderByOption;
		return this;
	}
	
	private String preparePropertyPath(Path<?> path) {

		Path<?> root = path.getRoot();

		return root == null || path.equals(root) ? path.toString()
				: path.toString().substring(root.toString().length() + 1);
	}
	
	private BooleanExpression getBooleanExpression() throws ODataApplicationException {

		if(filterOption != null) {
		
			Expression filterExpression = filterOption.getExpression();
			QueryDslExpressionVisitor expressionVisitor = new QueryDslExpressionVisitor(clazz);
			
			try {
				com.querydsl.core.types.Expression<?> expression = filterExpression.accept(expressionVisitor);
				BooleanExpression exp = (BooleanExpression) expression;
				
				return exp;
				
			} catch (ExpressionVisitException | ODataApplicationException e) {
				LOG.error(e.getMessage(), e);
				throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
			}
		}
		
		return (BooleanExpression) new BooleanBuilder().getValue();
	}
	
	private OrderSpecifier<?>[] getOrderSpecifiers() throws ODataApplicationException {
		if(orderByOption != null) {
			List<OrderByItem> orderByItems = orderByOption.getOrders();
			List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
			for(OrderByItem orderByItem : orderByItems) {
				Expression orderExpression = orderByItem.getExpression();
				boolean isDescending = orderByItem.isDescending();
				QueryDslExpressionVisitor expressionVisitor = new QueryDslExpressionVisitor(clazz);
				
				try {
					com.querydsl.core.types.Expression<?> expression = orderExpression.accept(expressionVisitor);
					if(expression instanceof ComparableExpressionBase<?>) {
						ComparableExpressionBase<?> comparableExpression = (ComparableExpressionBase<?>) expression;
						orderSpecifiers.add(isDescending ? comparableExpression.desc() : comparableExpression.asc());
					}
				} catch (ExpressionVisitException | ODataApplicationException e) {
					LOG.error(e.getMessage(), e);
					throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
				}
			}
			return orderSpecifiers.toArray(new OrderSpecifier<?>[orderSpecifiers.size()]);
		}
		return null;
	}
	
	public QuerydslQueryBuilder setQueryOptions(QueryOptions queryOptions) {

		this.queryOptions = queryOptions;
		
		if(queryOptions != null) {
			
			queryOptions.getFilterOption().ifPresent(option -> {
				this.filterOption = option;
			});
			
			queryOptions.getOrderByOption().ifPresent(option -> {
				this.orderByOption = option;
			});
		}
		
		return this;
	}
}
