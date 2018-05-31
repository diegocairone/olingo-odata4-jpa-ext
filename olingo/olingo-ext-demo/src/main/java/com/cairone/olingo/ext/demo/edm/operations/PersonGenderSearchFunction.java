package com.cairone.olingo.ext.demo.edm.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.edm.enums.GenderEnum;
import com.cairone.olingo.ext.demo.edm.resources.PersonEdm;
import com.cairone.olingo.ext.jpa.annotations.EdmFunction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;

@Component
@EdmFunction(namespace = AppDemoConstants.NAME_SPACE, name = "SearchPersonsByGenderFunction", isBound = false, entitySetPath = "People")
@EdmReturnType(type = "Collection(Person)")
public class PersonGenderSearchFunction implements Operation<List<PersonEdm>> {

	private static final Logger LOG = LoggerFactory.getLogger(PersonGenderSearchFunction.class);
	
	@EdmParameter private GenderEnum gender = null;
	
	@Override
	public List<PersonEdm> doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		LOG.debug("Filtering by gender: {}", gender);
		
		List<PersonEdm> personEdms = new ArrayList<>();
		return personEdms;
	}
}
