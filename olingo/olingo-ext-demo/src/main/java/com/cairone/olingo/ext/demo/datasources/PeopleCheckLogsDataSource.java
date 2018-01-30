package com.cairone.olingo.ext.demo.datasources;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.olingo.ext.demo.dtos.PersonCheckLogFrmDto;
import com.cairone.olingo.ext.demo.dtos.validators.PersonCheckLogFrmDtoValidator;
import com.cairone.olingo.ext.demo.edm.resources.PersonCheckLogEdm;
import com.cairone.olingo.ext.demo.entities.PersonCheckLogEntity;
import com.cairone.olingo.ext.demo.exceptions.ODataBadRequestException;
import com.cairone.olingo.ext.demo.services.PersonCheckLogService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.demo.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class PeopleCheckLogsDataSource extends AbstractDataSource {

	private static final String ENTITY_SET_NAME = "PeopleCheckLogs";

	@Autowired private PersonCheckLogService personCheckLogService = null;
	@Autowired private PersonCheckLogFrmDtoValidator personCheckLogFrmDtoValidator = null;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object create(Object entity, Object parentEntity) throws ODataApplicationException {

		if(entity instanceof PersonCheckLogEdm) {
			
			PersonCheckLogEdm personCheckLog = (PersonCheckLogEdm) entity;
			PersonCheckLogFrmDto personCheckLogFrmDto = new PersonCheckLogFrmDto(personCheckLog);
			
			try
			{
				ValidatorUtil.validate(personCheckLogFrmDtoValidator, messageSource, personCheckLogFrmDto);			
				PersonCheckLogEntity personCheckLogEntity = personCheckLogService.save(personCheckLogFrmDto);
				
				return new PersonCheckLogEdm(personCheckLogEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH PERSON CHECK LOG ENTITY");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, Object parentEntity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

		if(entity instanceof PersonCheckLogEdm) {

			Long eventID = Long.valueOf( keyPredicateMap.get("Id").getText() );
			
			PersonCheckLogEdm personCheckLog = (PersonCheckLogEdm) entity;
			PersonCheckLogFrmDto personCheckLogFrmDto = new PersonCheckLogFrmDto(personCheckLog);
			
			personCheckLogFrmDto.setId(eventID);
			
			try
			{
				PersonCheckLogEntity personCheckLogEntity = personCheckLogService.findOne(eventID);

    			if(!isPut) {
    				if(personCheckLogFrmDto.getPersonId() == null && !propertiesInJSON.contains("Person")) personCheckLogFrmDto.setPersonId(personCheckLogEntity.getPerson().getId());
    				if(personCheckLogFrmDto.getCheckType() == null && !propertiesInJSON.contains("CheckType")) personCheckLogFrmDto.setCheckType(personCheckLogEntity.getCheckType());
    				if(personCheckLogFrmDto.getDatetime() == null && !propertiesInJSON.contains("Moment")) personCheckLogFrmDto.setDatetime(personCheckLogEntity.getDatetime());
    			}
				
    			ValidatorUtil.validate(personCheckLogFrmDtoValidator, messageSource, personCheckLogFrmDto);
    			personCheckLogEntity = personCheckLogService.save(personCheckLogFrmDto);
				
				return new PersonCheckLogEdm(personCheckLogEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH PERSON CHECK LOG ENTITY");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap, Object parentEntity) throws ODataApplicationException {

		Long eventID = Long.valueOf( keyPredicateMap.get("Id").getText() );

		try {
			PersonCheckLogEntity personCheckLogEntity = personCheckLogService.findOne(eventID);
			personCheckLogService.delete(personCheckLogEntity);
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}

		return null;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption, Object parentEntity) throws ODataApplicationException {

		Long eventID = Long.valueOf( keyPredicateMap.get("Id").getText() );
		
		try {
			PersonCheckLogEntity personCheckLogEntity = personCheckLogService.findOne(eventID);
			PersonCheckLogEdm personCheckLogEdm = new PersonCheckLogEdm(personCheckLogEntity);
			
			return personCheckLogEdm;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption, Object parentEntity) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(PersonCheckLogEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<PersonCheckLogEntity> personCheckLogEntities = JPQLQuery.execute(entityManager, query);
		List<PersonCheckLogEdm> personCheckLogEdms = personCheckLogEntities.stream()
			.map(entity -> { 
				PersonCheckLogEdm personCheckLogEdm = new PersonCheckLogEdm(entity);
				return personCheckLogEdm;
			})
			.collect(Collectors.toList());
		
		return personCheckLogEdms;
	}
}
