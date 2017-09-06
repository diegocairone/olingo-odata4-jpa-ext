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

import com.cairone.olingo.ext.demo.dtos.FormFrmDto;
import com.cairone.olingo.ext.demo.dtos.validators.FormFrmDtoValidator;
import com.cairone.olingo.ext.demo.edm.resources.FormEdm;
import com.cairone.olingo.ext.demo.entities.FormEntity;
import com.cairone.olingo.ext.demo.exceptions.ODataBadRequestException;
import com.cairone.olingo.ext.demo.services.FormService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.demo.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class FormsDataSource extends AbstractDataSource {

	private static final String ENTITY_SET_NAME = "Forms";

	@Autowired private FormService formService = null;
	@Autowired private FormFrmDtoValidator formFrmDtoValidator = null;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object create(Object entity) throws ODataApplicationException {

		if(entity instanceof FormEdm) {
			
			FormEdm form = (FormEdm) entity;
			FormFrmDto formFrmDto = new FormFrmDto(form);
			
			try
			{
				ValidatorUtil.validate(formFrmDtoValidator, messageSource, formFrmDto);			
				FormEntity formEntity = formService.save(formFrmDto);
				
				return new FormEdm(formEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH COUNTRY ENTITY");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

		if(entity instanceof FormEdm) {

			String formID = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("Name").getText() );
			
			FormEdm form = (FormEdm) entity;
			FormFrmDto formFrmDto = new FormFrmDto(form);
			
			formFrmDto.setId(formID);
			
			try
			{
				FormEntity formEntity = formService.findOne(formID);

    			if(!isPut) {
    				if(formFrmDto.getName() == null && !propertiesInJSON.contains("Name")) formFrmDto.setName(formEntity.getName());
    			}
				
				ValidatorUtil.validate(formFrmDtoValidator, messageSource, formFrmDto);			
				formEntity = formService.save(formFrmDto);
				
				return new FormEdm(formEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH COUNTRY ENTITY");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {
		
		String formID = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("Name").getText() );

		try {
			FormEntity formEntity = formService.findOne(formID);
			formService.delete(formEntity);
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}

		return null;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption) throws ODataApplicationException {

		String formID = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("Name").getText() );
		
		try {
			FormEntity formEntity = formService.findOne(formID);
			FormEdm formEdm = new FormEdm(formEntity);
			
			return formEdm;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataApplicationException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(FormEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<FormEntity> formEntities = JPQLQuery.execute(entityManager, query);
		List<FormEdm> formEdms = formEntities.stream()
			.map(entity -> { 
				FormEdm formEdm = new FormEdm(entity);
				return formEdm;
			})
			.collect(Collectors.toList());
		
		return formEdms;
	}
}
