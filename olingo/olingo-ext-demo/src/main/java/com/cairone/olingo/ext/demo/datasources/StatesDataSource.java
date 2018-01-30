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

import com.cairone.olingo.ext.demo.dtos.StateFrmDto;
import com.cairone.olingo.ext.demo.dtos.validators.StateFrmDtoValidator;
import com.cairone.olingo.ext.demo.edm.resources.CountryEdm;
import com.cairone.olingo.ext.demo.edm.resources.StateEdm;
import com.cairone.olingo.ext.demo.entities.StateEntity;
import com.cairone.olingo.ext.demo.exceptions.ODataBadRequestException;
import com.cairone.olingo.ext.demo.services.StateService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.demo.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class StatesDataSource extends AbstractConditionalDataSource {

	private static final String ENTITY_SET_NAME = "States";
	
	@Autowired private StateService stateService = null;
	@Autowired private StateFrmDtoValidator stateFrmDtoValidator = null;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object create(Object entity) throws ODataApplicationException {

		if(entity instanceof StateEdm) {
			
			StateEdm state = (StateEdm) entity;
			StateFrmDto stateFrmDto = new StateFrmDto(state);
			
			try
			{
				ValidatorUtil.validate(stateFrmDtoValidator, messageSource, stateFrmDto);			
				StateEntity stateEntity = stateService.save(stateFrmDto);
				
				return new StateEdm(stateEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH STATE ENTITY");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

		if(entity instanceof StateEdm) {

			Integer stateID = Integer.valueOf( keyPredicateMap.get("Id").getText() );
			
			StateEdm state = (StateEdm) entity;
			StateFrmDto stateFrmDto = new StateFrmDto(state);
			
			stateFrmDto.setId(stateID);
			
			try
			{
				StateEntity stateEntity = stateService.findOne(stateID);

    			if(!isPut) {
    				if(stateFrmDto.getName() == null && !propertiesInJSON.contains("Name")) stateFrmDto.setName(stateEntity.getName());
    				if(stateFrmDto.getCountryId() == null && !propertiesInJSON.contains("Country")) stateFrmDto.setCountryId(stateEntity.getCountry().getId());
    			}
				
				ValidatorUtil.validate(stateFrmDtoValidator, messageSource, stateFrmDto);			
				stateEntity = stateService.save(stateFrmDto);
				
				return new StateEdm(stateEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH STATE ENTITY");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

		Integer stateID = Integer.valueOf( keyPredicateMap.get("Id").getText() );

		try {
			StateEntity stateEntity = stateService.findOne(stateID);
			stateService.delete(stateEntity);
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}

		return null;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {

		Integer stateID = Integer.valueOf( keyPredicateMap.get("Id").getText() );
		
		try {
			StateEntity stateEntity = stateService.findOne(stateID);
			StateEdm stateEdm = new StateEdm(stateEntity);
			
			return stateEdm;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(StateEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<StateEntity> stateEntities = JPQLQuery.execute(entityManager, query);
		List<StateEdm> stateEdms = stateEntities.stream()
			.map(entity -> { 
				StateEdm stateEdm = new StateEdm(entity);
				return stateEdm;
			})
			.collect(Collectors.toList());
		
		return stateEdms;
	}
	
	@Override
	public Iterable<?> readConditioned(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption, Object conditionalEntity) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(StateEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<StateEntity> stateEntities = JPQLQuery.execute(entityManager, query);
		CountryEdm target = (CountryEdm) conditionalEntity;
		
		List<StateEdm> stateEdms = stateEntities.stream()
			.filter(stateEntity -> {
				return stateEntity.getCountry().getId().equals(target.getId());
			})
			.map(stateEntity -> { 
				StateEdm stateEdm = new StateEdm(stateEntity);
				return stateEdm;
			})
			.collect(Collectors.toList());
		
		return stateEdms;
	}
}
