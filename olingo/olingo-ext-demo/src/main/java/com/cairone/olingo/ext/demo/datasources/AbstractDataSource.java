package com.cairone.olingo.ext.demo.datasources;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.cairone.olingo.ext.jpa.interfaces.DataSource;

public abstract class AbstractDataSource implements DataSource {
	
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractDataSource.class);
	
	@Autowired protected MessageSource messageSource = null;
	@PersistenceContext protected EntityManager entityManager;
	
	@Override
	public abstract String isSuitableFor();

	@Override
	public abstract Object create(Object entity) throws ODataApplicationException;

	@Override
	public abstract Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException;

	@Override
	public abstract Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException;

	@Override
	public abstract Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException;

	@Override
	public abstract Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException;
}
