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

import com.cairone.olingo.ext.demo.dtos.FormTypeFrmDto;
import com.cairone.olingo.ext.demo.dtos.validators.FormTypeFrmDtoValidator;
import com.cairone.olingo.ext.demo.edm.resources.FormTypeEdm;
import com.cairone.olingo.ext.demo.entities.FormTypeEntity;
import com.cairone.olingo.ext.demo.exceptions.ODataBadRequestException;
import com.cairone.olingo.ext.demo.services.FormTypeService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.demo.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.interfaces.QueryOptions;
import com.cairone.olingo.ext.jpa.query.QuerydslQuery;
import com.cairone.olingo.ext.jpa.query.QuerydslQueryBuilder;

@Component
public class FormTypesDataSource extends AbstractDataSource<FormTypeEdm> {

	private static final String ENTITY_SET_NAME = "FormTypes";

	@Autowired private FormTypeService formTypeService = null;
	@Autowired private FormTypeFrmDtoValidator formTypeFrmDtoValidator = null;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public FormTypeEdm create(FormTypeEdm entity) throws ODataApplicationException {

		if(entity instanceof FormTypeEdm) {
			
			FormTypeEdm formType = (FormTypeEdm) entity;
			FormTypeFrmDto formTypeFrmDto = new FormTypeFrmDto(formType);
			
			try
			{
				ValidatorUtil.validate(formTypeFrmDtoValidator, messageSource, formTypeFrmDto);			
				FormTypeEntity formTypeEntity = formTypeService.save(formTypeFrmDto);
				
				return new FormTypeEdm(formTypeEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH FORM TYPE ENTITY");
	}

	@Override
	public FormTypeEdm update(Map<String, UriParameter> keyPredicateMap, FormTypeEdm entity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {

		if(entity instanceof FormTypeEdm) {

			Integer formTypeID = Integer.valueOf( keyPredicateMap.get("Id").getText() );
			
			FormTypeEdm formType = (FormTypeEdm) entity;
			FormTypeFrmDto formTypeFrmDto = new FormTypeFrmDto(formType);
			
			formTypeFrmDto.setId(formTypeID);
			
			try
			{
				FormTypeEntity formTypeEntity = formTypeService.findOne(formTypeID);

    			if(!isPut) {
    				if(formTypeFrmDto.getName() == null && !propertiesInJSON.contains("Name")) formTypeFrmDto.setName(formTypeEntity.getName());
    			}
				
				ValidatorUtil.validate(formTypeFrmDtoValidator, messageSource, formTypeFrmDto);			
				formTypeEntity = formTypeService.save(formTypeFrmDto);
				
				return new FormTypeEdm(formTypeEntity);
				
			} catch (Exception e) {
				throw OdataExceptionParser.parse(e);
			}
		}
		
		throw new ODataBadRequestException("REQUEST DATA DOES NOT MATCH FORM TYPE ENTITY");
	}

	@Override
	public FormTypeEdm delete(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {
		
		Integer formTypeID = Integer.valueOf( keyPredicateMap.get("Id").getText() );

		try {
			FormTypeEntity formTypeEntity = formTypeService.findOne(formTypeID);
			formTypeService.delete(formTypeEntity);
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}

		return null;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption, Object parentEntity) throws ODataApplicationException {

		Integer formTypeID = Integer.valueOf( keyPredicateMap.get("Id").getText() );
		
		try {
			FormTypeEntity formTypeEntity = formTypeService.findOne(formTypeID);
			FormTypeEdm formTypeEdm = new FormTypeEdm(formTypeEntity);
			
			return formTypeEdm;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(QueryOptions queryOptions, Object parentEntity) throws ODataApplicationException {

		QuerydslQuery query = new QuerydslQueryBuilder()
				.setClazz(FormTypeEdm.class)
				.setQueryOptions(queryOptions)
				.build();
	
		List<FormTypeEntity> formTypeEntities = QuerydslQuery.execute(formTypeService.getFormTypeRepository(), query);
		List<FormTypeEdm> formTypeEdms = formTypeEntities.stream()
			.map(entity -> { 
				FormTypeEdm formTypeEdm = new FormTypeEdm(entity);
				return formTypeEdm;
			})
			.collect(Collectors.toList());
		
		return formTypeEdms;
	}
}
