package com.cairone.olingo.ext.jpa.query;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.cairone.olingo.ext.jpa.processors.BaseProcessor;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;

public class QuerydslQuery {

	protected static final Logger LOG = LoggerFactory.getLogger(BaseProcessor.class);
	
	private BooleanExpression booleanExpression = null;
	private OrderSpecifier<?>[] orderSpecifiers = null;
	
	public QuerydslQuery(BooleanExpression booleanExpression, OrderSpecifier<?>[] orderSpecifiers) {
		super();
		this.booleanExpression = booleanExpression;
		this.orderSpecifiers = orderSpecifiers;
	}

	public BooleanExpression getBooleanExpression() {
		return booleanExpression;
	}

	public OrderSpecifier<?>[] getOrderSpecifiers() {
		return orderSpecifiers;
	}

	@Override
	public String toString() {
		return "DslQuery [booleanExpression=" + booleanExpression + ", orderSpecifiers="
				+ Arrays.toString(orderSpecifiers) + "]";
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> execute(QuerydslPredicateExecutor<?> queryDslPredicateExecutor, QuerydslQuery dslQuery) throws ODataApplicationException {
		
		try {
			return dslQuery.getOrderSpecifiers() == null
					? (List<T>) queryDslPredicateExecutor.findAll(dslQuery.getBooleanExpression())
					: (List<T>) queryDslPredicateExecutor.findAll(dslQuery.getBooleanExpression(), dslQuery.getOrderSpecifiers());
		} catch(DataAccessException e) {
			LOG.error(e.getMessage(), e);
			throw new ODataApplicationException(
					e.getMessage(), 
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
					Locale.ENGLISH,
					e);
		}
	}
}
