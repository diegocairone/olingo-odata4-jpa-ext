package com.cairone.olingo.ext.jpa.interfaces;

import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;

public interface Operation<T> {

	T doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException;
}
