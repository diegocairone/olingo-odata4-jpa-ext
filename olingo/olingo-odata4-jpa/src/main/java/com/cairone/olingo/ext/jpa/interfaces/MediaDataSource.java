package com.cairone.olingo.ext.jpa.interfaces;

import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

public interface MediaDataSource {

	String isSuitableFor();
	
	byte[] findMediaResource(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException;
	Object createMediaResource(byte[] binary) throws ODataApplicationException;
	void updateMediaResource(Map<String, UriParameter> keyPredicateMap, byte[] binary) throws ODataApplicationException;
	//void deleteMediaResource(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException;
	
}
