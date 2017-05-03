package com.cairone.olingo.ext.jpa.interfaces;

import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;

public interface DataSourceProvider {

	String isSuitableFor();
	DataSource getDataSource();
	
	Object readFromKey(Map<String, UriParameter> keyPredicateMap) throws ODataException;
	Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataException;
	
}
