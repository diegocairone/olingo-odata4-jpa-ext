package com.cairone.olingo.ext.demo.edm.operations;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.entities.PersonEntity;
import com.cairone.olingo.ext.demo.services.PersonService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.jpa.annotations.EdmFunction;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;

@Component
@EdmFunction(namespace = AppDemoConstants.NAME_SPACE, name = "GetAgeFunction", isBound = true, entitySetPath = "People")
@EdmReturnType(type = "Edm.Int32")
public class GetAgeFunction implements Operation<Integer> {

	@Autowired private PersonService personService = null;
	
	@Override
	public Integer doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		Integer personID = Integer.valueOf( keyPredicateMap.get("Id").getText() );

		try {
			PersonEntity personEntity = personService.findOne(personID);
			LocalDate birthDate = personEntity.getBirthDate();
			
			if(birthDate != null) {
				int years = Period.between(birthDate, LocalDate.now()).getYears();
				return years;
			}
			
			return 0;
			
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}
}
