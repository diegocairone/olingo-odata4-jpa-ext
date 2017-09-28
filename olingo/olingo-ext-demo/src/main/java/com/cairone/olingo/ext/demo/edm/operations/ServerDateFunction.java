package com.cairone.olingo.ext.demo.edm.operations;

import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.stereotype.Component;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.edm.resources.ServerDateEdm;
import com.cairone.olingo.ext.jpa.annotations.EdmFunction;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;

@Component
@EdmFunction(namespace = AppDemoConstants.NAME_SPACE, name = "GetServerDateFunction", isBound = false, entitySetPath = "ServerDates")
@EdmReturnType(type = "ServerDate")
public class ServerDateFunction implements Operation<ServerDateEdm> {

	@Override
	public ServerDateEdm doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {
		return new ServerDateEdm();
	}
}
