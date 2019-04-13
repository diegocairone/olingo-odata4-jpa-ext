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

import com.cairone.olingo.ext.demo.dtos.StateFrmDto;
import com.cairone.olingo.ext.demo.dtos.validators.StateFrmDtoValidator;
import com.cairone.olingo.ext.demo.edm.resources.CountryEdm;
import com.cairone.olingo.ext.demo.edm.resources.StateEdm;
import com.cairone.olingo.ext.demo.entities.StateEntity;
import com.cairone.olingo.ext.demo.exceptions.ODataBadRequestException;
import com.cairone.olingo.ext.demo.services.StateService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.demo.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.interfaces.QueryOptions;
import com.cairone.olingo.ext.jpa.query.QuerydslQuery;
import com.cairone.olingo.ext.jpa.query.QuerydslQueryBuilder;

@Component
public class StatesDataSource extends AbstractDataSource<StateEdm> {

	private static final String ENTITY_SET_NAME = "States";
	
	@Autowired private StateService stateService = null;
	@Autowired private StateFrmDtoValidator stateFrmDtoValidator = null;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public StateEdm create(StateEdm entity) throws ODataApplicationException {

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
	public StateEdm update(Map<String, UriParameter> keyPredicateMap, StateEdm entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

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
	public StateEdm delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

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
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption, Object parentEntity) throws ODataApplicationException {

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
	public Iterable<?> readAll(QueryOptions queryOptions, Object parentEntity) throws ODataApplicationException {

		QuerydslQuery query = new QuerydslQueryBuilder()
			.setClazz(StateEdm.class)
			.setQueryOptions(queryOptions)
			.build();
	
		List<StateEntity> stateEntities = QuerydslQuery.execute(stateService.getStateRepository(), query);
		CountryEdm target = parentEntity == null ? null : (CountryEdm) parentEntity;
		
		List<StateEdm> stateEdms = stateEntities.stream()
			.filter(stateEntity -> target == null ? true : stateEntity.getCountry().getId().equals(target.getId()))
			.map(stateEntity -> new StateEdm(stateEntity))
			.collect(Collectors.toList());
		
		return stateEdms;
	}
}
