package com.cairone.olingo.ext.demo.datasources;

import java.util.List;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.stereotype.Component;

@Component
public class CustomersDataSource extends AbstractDataSource {

	private static final String ENTITY_SET_NAME = "Customers";
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object create(Object entity, Object superentity) throws ODataApplicationException {
		return null;
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, Object superentity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {
		return null;
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap, Object superentity) throws ODataApplicationException {
		return null;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption, Object superentity) throws ODataApplicationException {
		return null;
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption, Object parentEntity) throws ODataApplicationException {
		return null;
	}
}
