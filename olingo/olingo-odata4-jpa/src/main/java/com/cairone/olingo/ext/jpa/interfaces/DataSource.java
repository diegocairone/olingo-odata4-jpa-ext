package com.cairone.olingo.ext.jpa.interfaces;

import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;

public interface DataSource {

	Object create(Object entity) throws ODataException;
	Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException;
	Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException;
}
