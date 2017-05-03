package com.cairone.odataexample.datasources;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.dtos.LocalidadFrmDto;
import com.cairone.odataexample.dtos.validators.LocalidadFrmDtoValidator;
import com.cairone.odataexample.edm.resources.LocalidadEdm;
import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.services.LocalidadService;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.DataSourceProvider;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class LocalidadDataSource implements DataSourceProvider, DataSource {

	private static Logger logger = LoggerFactory.getLogger(LocalidadDataSource.class);

	private static final String ENTITY_SET_NAME = "Localidades";

	@Autowired private LocalidadService localidadService = null;
	@Autowired private LocalidadFrmDtoValidator localidadFrmDtoValidator = null;

	@Autowired
	private MessageSource messageSource = null;

	@Autowired
    private EntityManagerFactory entityManagerFactory;
		
	@Override
	public Object create(Object entity) throws ODataException {
		
		if(entity instanceof LocalidadEdm) {
			
			LocalidadEdm localidadEdm = (LocalidadEdm) entity;
			LocalidadFrmDto localidadFrmDto = new LocalidadFrmDto(localidadEdm);
			
			try {
				ValidatorUtil.validate(localidadFrmDtoValidator, messageSource, localidadFrmDto);
				LocalidadEntity localidadEntity = localidadService.nuevo(localidadFrmDto);
				return new LocalidadEdm(localidadEntity);
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		}

		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD LOCALIDAD", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {
		
    	if(entity instanceof LocalidadEdm) {
    		
    		LocalidadEdm localidad = (LocalidadEdm) entity;
    		LocalidadFrmDto localidadFrmDto;
    		
    		Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    		Integer provinciaID = Integer.valueOf( keyPredicateMap.get("provinciaId").getText() );
    		Integer localidadID = Integer.valueOf( keyPredicateMap.get("localidadId").getText() );
        	
    		if(isPut) {
    			localidadFrmDto = new LocalidadFrmDto(localidad);
    			localidadFrmDto.setLocalidadId(localidadID);
    			localidadFrmDto.setProvinciaId(provinciaID);
    			localidadFrmDto.setPaisId(paisID);
    		} else {
	    		LocalidadEntity localidadEntity = localidadService.buscarPorID(paisID, provinciaID, localidadID);
	    		
	    		if(localidadEntity == null) {
	    			throw new ODataApplicationException(
	    				String.format("LA LOCALIDAD CON ID (PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s) NO EXITE", paisID, provinciaID, localidadID), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    		}
	    		
	    		// *** CAMPO << NOMBRE >>
	    		
	    		if(propertiesInJSON.contains("nombre")) {
	    			localidadEntity.setNombre(localidad.getNombre() == null || localidad.getNombre().trim().isEmpty() ? null : localidad.getNombre().trim().toUpperCase());
	    		}
	    		
	    		localidadFrmDto = new LocalidadFrmDto(localidadEntity);
    		}
    		
			try {
				ValidatorUtil.validate(localidadFrmDtoValidator, messageSource, localidadFrmDto);
				return new LocalidadEdm( localidadService.actualizar(localidadFrmDto) );
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
    	}
    	
    	throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD LOCALIDAD", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {

    	Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    	Integer provinciaID = Integer.valueOf( keyPredicateMap.get("provinciaId").getText() );
    	Integer localidadID = Integer.valueOf( keyPredicateMap.get("localidadId").getText() );
    	
    	try {
			localidadService.borrar(paisID, provinciaID, localidadID);
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
		
		Integer paisID = Integer.valueOf( keyPredicateMap.get("paisId").getText() );
    	Integer provinciaID = Integer.valueOf( keyPredicateMap.get("provinciaId").getText() );
    	Integer localidadID = Integer.valueOf( keyPredicateMap.get("localidadId").getText() );
    	
    	LocalidadEntity localidadEntity = localidadService.buscarPorID(paisID, provinciaID, localidadID);
    	LocalidadEdm localidadEdm = localidadEntity == null ? null : new LocalidadEdm(localidadEntity);
    	
    	return localidadEdm;
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(LocalidadEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<LocalidadEntity> localidadEntities = executeQueryListResult(query);
		List<LocalidadEdm> localidadEdms = localidadEntities.stream().map(entity -> { return new LocalidadEdm(entity); }).collect(Collectors.toList());
		
		return localidadEdms;
	}

    @SuppressWarnings("unchecked")
	protected <T> List<T> executeQueryListResult(JPQLQuery jpaQuery) {

        EntityManager em = entityManagerFactory.createEntityManager();

        String queryString = jpaQuery.getQueryString();

    	logger.info("JPQL: {}", queryString);
    	
        Query query = em.createQuery(queryString);
        Map<String, Object> queryParams = jpaQuery.getQueryParams();

        try {
        	em.getTransaction().begin();

            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
