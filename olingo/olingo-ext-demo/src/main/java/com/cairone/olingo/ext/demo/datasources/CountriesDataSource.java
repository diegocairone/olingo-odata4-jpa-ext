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

import com.cairone.olingo.ext.demo.dtos.CountryFrmDto;
import com.cairone.olingo.ext.demo.dtos.validators.CountryFrmDtoValidator;
import com.cairone.olingo.ext.demo.edm.resources.CountryEdm;
import com.cairone.olingo.ext.demo.entities.CountryEntity;
import com.cairone.olingo.ext.demo.exceptions.ODataBadRequestException;
import com.cairone.olingo.ext.demo.services.CountryService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.demo.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class CountriesDataSource extends AbstractDataSource {

	private static final String ENTITY_SET_NAME = "Countries";

	@Autowired private CountryService countryService = null;
	@Autowired private CountryFrmDtoValidator countryFrmDtoValidator = null;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object create(Object entity) throws ODataApplicationException {

		if(entity instanceof CountryEdm) {
			
			CountryEdm country = (CountryEdm) entity;
			CountryFrmDto countryFrmDto = new CountryFrmDto(country);
			
			try
			{
				ValidatorUtil.validate(countryFrmDtoValidator, messageSource, countryFrmDto);			
				CountryEntity countryEntity = countryService.save(countryFrmDto);
				
				return new CountryEdm(countryEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH COUNTRY ENTITY");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

		if(entity instanceof CountryEdm) {

			Integer countryID = Integer.valueOf( keyPredicateMap.get("Id").getText() );
			
			CountryEdm country = (CountryEdm) entity;
			CountryFrmDto countryFrmDto = new CountryFrmDto(country);
			
			countryFrmDto.setId(countryID);
			
			try
			{
				CountryEntity countryEntity = countryService.findOne(countryID);

    			if(!isPut) {
    				if(countryFrmDto.getName() == null && !propertiesInJSON.contains("Name")) countryFrmDto.setName(countryEntity.getName());
    			}
				
				ValidatorUtil.validate(countryFrmDtoValidator, messageSource, countryFrmDto);			
				countryEntity = countryService.save(countryFrmDto);
				
				return new CountryEdm(countryEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH COUNTRY ENTITY");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {
		
		Integer countryID = Integer.valueOf( keyPredicateMap.get("Id").getText() );

		try {
			CountryEntity countryEntity = countryService.findOne(countryID);
			countryService.delete(countryEntity);
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}

		return null;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {

		Integer countryID = Integer.valueOf( keyPredicateMap.get("Id").getText() );
		
		try {
			CountryEntity countryEntity = countryService.findOne(countryID);
			CountryEdm countryEdm = new CountryEdm(countryEntity);
			
			return countryEdm;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(CountryEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<CountryEntity> countryEntities = JPQLQuery.execute(entityManager, query);
		List<CountryEdm> countryEdms = countryEntities.stream()
			.map(entity -> { 
				CountryEdm countryEdm = new CountryEdm(entity);
				return countryEdm;
			})
			.collect(Collectors.toList());
		
		return countryEdms;
	}
}
