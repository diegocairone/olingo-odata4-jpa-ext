package com.cairone.olingo.ext.demo.edm.operations;

import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.edm.resources.PersonAddressEdm;
import com.cairone.olingo.ext.demo.edm.resources.StateEdm;
import com.cairone.olingo.ext.demo.entities.PersonEntity;
import com.cairone.olingo.ext.demo.services.PersonService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.jpa.annotations.EdmFunction;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;

@Component
@EdmFunction(namespace = AppDemoConstants.NAME_SPACE, name = "GetPersonAddressFunction", isBound = true, entitySetPath = "People")
@EdmReturnType(type = "Address")
public class GetPersonAddressFunction implements Operation<PersonAddressEdm> {

	@Autowired private PersonService personService = null;
	
	@Override
	public PersonAddressEdm doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer personID = Integer.valueOf( keyPredicateMap.get("Id").getText() );

		try {
			PersonEntity personEntity = personService.findOne(personID);
			PersonAddressEdm personAddressEdm = 
					new PersonAddressEdm(
							personEntity.getAddressStreet(), 
							personEntity.getAddressNumber(), 
							personEntity.getState() == null ? null : new StateEdm(personEntity.getState()));
			
			return personAddressEdm;
			
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}
}
