package com.cairone.olingo.ext.demo.datasources;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.olingo.ext.demo.dtos.PersonFrmDto;
import com.cairone.olingo.ext.demo.dtos.validators.PersonFrmDtoValidator;
import com.cairone.olingo.ext.demo.edm.enums.RegionEnum;
import com.cairone.olingo.ext.demo.edm.resources.PersonEdm;
import com.cairone.olingo.ext.demo.entities.PersonEntity;
import com.cairone.olingo.ext.demo.exceptions.ODataBadRequestException;
import com.cairone.olingo.ext.demo.repositories.PersonRepository;
import com.cairone.olingo.ext.demo.services.PersonService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.demo.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.interfaces.QueryOptions;
import com.cairone.olingo.ext.jpa.query.QuerydslQuery;
import com.cairone.olingo.ext.jpa.query.QuerydslQueryBuilder;

@Component
public class PeopleDataSource extends AbstractDataSource<PersonEdm> {

	private static final String ENTITY_SET_NAME = "People";

	@Autowired private PersonService personService = null;
	@Autowired private PersonFrmDtoValidator personFrmDtoValidator = null;
	
	@Autowired private PersonRepository personRepository = null;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public PersonEdm create(PersonEdm entity) throws ODataApplicationException {

		if(entity instanceof PersonEdm) {
			
			PersonEdm person = (PersonEdm) entity;
			PersonFrmDto personFrmDto = new PersonFrmDto(person);
			
			try
			{
				ValidatorUtil.validate(personFrmDtoValidator, messageSource, personFrmDto);			
				PersonEntity personEntity = personService.save(personFrmDto);
				
				return new PersonEdm(personEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH PERSON ENTITY");
	}

	@Override
	public PersonEdm update(Map<String, UriParameter> keyPredicateMap, PersonEdm entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

		if(entity instanceof PersonEdm) {

			Integer personID = Integer.valueOf( keyPredicateMap.get("Id").getText() );
			
			PersonEdm person = (PersonEdm) entity;
			PersonFrmDto personFrmDto = new PersonFrmDto(person);
			
			personFrmDto.setId(personID);
			
			try
			{
				PersonEntity personEntity = personService.findOne(personID);

    			if(!isPut) {
    				if(personFrmDto.getName() == null && !propertiesInJSON.contains("Name")) personFrmDto.setName(personEntity.getName());
    				if(personFrmDto.getSurname() == null && !propertiesInJSON.contains("Surname")) personFrmDto.setSurname(personEntity.getSurname());
    				if(personFrmDto.getGender() == null && !propertiesInJSON.contains("Gender")) personFrmDto.setGender(personEntity.getGender());
    				if(personFrmDto.getRegion() == null && !propertiesInJSON.contains("Region")) personFrmDto.setRegion(personEntity.getRegion() == null ? null : RegionEnum.fromDb(personEntity.getRegion().getId()));
    				if(personFrmDto.getFormId() == null && !propertiesInJSON.contains("Form")) personFrmDto.setFormId(personEntity.getForm() == null ? null : personEntity.getForm().getId());
    				if(personFrmDto.getAddressStreet() == null && !propertiesInJSON.contains("Address/Name")) personFrmDto.setAddressStreet(personEntity.getAddressStreet() == null ? null : personEntity.getAddressStreet());
    				if(personFrmDto.getAddressNumber() == null && !propertiesInJSON.contains("Address/Number")) personFrmDto.setAddressNumber(personEntity.getAddressNumber() == null ? null : personEntity.getAddressNumber());
    			}
				
				ValidatorUtil.validate(personFrmDtoValidator, messageSource, personFrmDto);			
				personEntity = personService.save(personFrmDto);
				
				return new PersonEdm(personEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH PERSON ENTITY");
	}

	@Override
	public PersonEdm delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {
		
		Integer personID = Integer.valueOf( keyPredicateMap.get("Id").getText() );

		try {
			PersonEntity personEntity = personService.findOne(personID);
			personService.delete(personEntity);
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}

		return null;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption, Object parentEntity) throws ODataApplicationException {

		Integer personID = Integer.valueOf( keyPredicateMap.get("Id").getText() );
		
		try {
			PersonEntity personEntity = personService.findOne(personID);
			PersonEdm personEdm = new PersonEdm(personEntity);
			
			return personEdm;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(QueryOptions queryOptions, Object parentEntity) throws ODataApplicationException {
		
		QuerydslQuery query = new QuerydslQueryBuilder()
				.setClazz(PersonEdm.class)
				.setQueryOptions(queryOptions)
				.build();
		
		List<PersonEntity> personEntities = QuerydslQuery.execute(personRepository, query);
		List<PersonEdm> personEdms = personEntities.stream()
			.map(entity -> { 
				PersonEdm personEdm = new PersonEdm(entity);
				return personEdm;
			})
			.collect(Collectors.toList());
		
		return personEdms;
	}
}
