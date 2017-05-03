package com.cairone.odataexample.datasources;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.dtos.SectorFrmDto;
import com.cairone.odataexample.dtos.validators.SectorFrmDtoValidator;
import com.cairone.odataexample.edm.resources.SectorEdm;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.services.SectorService;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.DataSourceProvider;

@Component
public class SectorDataSource implements DataSourceProvider, DataSource {

	private static final String ENTITY_SET_NAME = "Sectores";

	@Autowired private SectorService sectorService = null;
	@Autowired private SectorFrmDtoValidator sectorFrmDtoValidator = null;
	
	@Autowired
	private MessageSource messageSource = null;

	@Override
	public Object create(Object entity) throws ODataException {

		if(entity instanceof SectorEdm) {
			
			SectorEdm sectorEdm = (SectorEdm) entity;
			SectorFrmDto sectorFrmDto = new SectorFrmDto(sectorEdm);
    		
			try {
				ValidatorUtil.validate(sectorFrmDtoValidator, messageSource, sectorFrmDto);
				SectorEntity sectorEntity = sectorService.nuevo(sectorFrmDto);
				return new SectorEdm(sectorEntity);
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		}
		
		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD SECTOR", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {

    	if(entity instanceof SectorEdm) {
    		
    		SectorEdm sectorEdm = (SectorEdm) entity;
    		SectorFrmDto sectorFrmDto;

        	Integer sectorID = Integer.valueOf( keyPredicateMap.get("id").getText() );
        	
    		if(isPut) {
    			sectorFrmDto = new SectorFrmDto(sectorEdm);
    			sectorFrmDto.setId(sectorID);
    		} else {
    			SectorEntity sectorEntity = sectorService.buscarPorID(sectorID);
	    		
	    		if(sectorEntity == null) {
	    			throw new ODataApplicationException(
	    					String.format("EL SECTOR CON ID %s NO EXITE", sectorID), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    		}
	    		
	    		// *** CAMPO << NOMBRE >>
	    		
	    		if(propertiesInJSON.contains("nombre")) {
	    			sectorEntity.setNombre(sectorEdm.getNombre() == null || sectorEdm.getNombre().trim().isEmpty() ? null : sectorEdm.getNombre().trim().toUpperCase());
	    		}
	    			    		
	    		sectorFrmDto = new SectorFrmDto(sectorEntity);
    		}
    		
			try {
				ValidatorUtil.validate(sectorFrmDtoValidator, messageSource, sectorFrmDto);
				return new SectorEdm( sectorService.actualizar(sectorFrmDto) );
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
    	}
    	
    	throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD SECTOR", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer sectorID = Integer.valueOf( keyPredicateMap.get("id").getText() );

    	try {
    		sectorService.borrar(sectorID);
		} catch (Exception e) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
    	
    	return null;
	}

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public DataSource getDataSource() {
		return this;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		Integer sectorID = Integer.valueOf( keyPredicateMap.get("id").getText() );
		
		SectorEntity sectorEntity = sectorService.buscarPorID(sectorID);
		SectorEdm sectorEdm = sectorEntity == null ? null : new SectorEdm(sectorEntity);
		
		return sectorEdm;
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataException {

		List<Sort.Order> orderByList = new ArrayList<Sort.Order>();
		
		if(orderByOption != null) {
			orderByOption.getOrders().forEach(orderByItem -> {
				
				Expression expression = orderByItem.getExpression();
				if(expression instanceof Member){
					
					UriInfoResource resourcePath = ((Member)expression).getResourcePath();
					UriResource uriResource = resourcePath.getUriResourceParts().get(0);
					
				    if (uriResource instanceof UriResourcePrimitiveProperty) {
				    	EdmProperty edmProperty = ((UriResourcePrimitiveProperty)uriResource).getProperty();
						Direction direction = orderByItem.isDescending() ? Direction.DESC : Direction.ASC;
						String property = edmProperty.getName();
						orderByList.add(new Order(direction, property));
				    }
				}
				
			});
		}
		
		return sectorService.ejecutarConsulta(null, orderByList).stream().map(e -> new SectorEdm(e)).collect(Collectors.toList());
	}
}
