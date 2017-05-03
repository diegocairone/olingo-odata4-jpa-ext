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

import com.cairone.odataexample.dtos.PaisFrmDto;
import com.cairone.odataexample.dtos.validators.PaisFrmDtoValidator;
import com.cairone.odataexample.edm.resources.PaisEdm;
import com.cairone.odataexample.entities.PaisEntity;
import com.cairone.odataexample.services.PaisService;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.DataSourceProvider;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;

@Component
public class PaisDataSource implements DataSource, DataSourceProvider {
	
	private static Logger logger = LoggerFactory.getLogger(PaisDataSource.class);
	
	private static final String ENTITY_SET_NAME = "Paises";
	
	@Autowired private PaisService paisService = null;
	@Autowired private PaisFrmDtoValidator paisFrmDtoValidator = null;
	
	@Autowired
    private EntityManagerFactory entityManagerFactory;
		
	@Autowired
	private MessageSource messageSource = null;
	
	@Override
	public Object create(Object entity) throws ODataException {
		
		if(entity instanceof PaisEdm) {
			
			PaisEdm paisEdm = (PaisEdm) entity;
    		PaisFrmDto paisFrmDto = new PaisFrmDto(paisEdm);
    		
			try {
				ValidatorUtil.validate(paisFrmDtoValidator, messageSource, paisFrmDto);
				PaisEntity paisEntity = paisService.nuevo(paisFrmDto);
				return new PaisEdm(paisEntity);
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		}
		
		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PAIS", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}
	
	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {
		
    	if(entity instanceof PaisEdm) {
    		
    		PaisEdm pais = (PaisEdm) entity;
    		PaisFrmDto paisFrmDto;

        	Integer paisID = Integer.valueOf( keyPredicateMap.get("id").getText() );
        	
    		if(isPut) {
    			paisFrmDto = new PaisFrmDto(pais);
    			paisFrmDto.setId(paisID);
    		} else {
	    		PaisEntity paisEntity = paisService.buscarPorID(paisID);
	    		
	    		if(paisEntity == null) {
	    			throw new ODataApplicationException(
	    					String.format("EL PAIS CON ID %s NO EXITE", pais.getId()), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    		}
	    		
	    		// *** CAMPO << NOMBRE >>
	    		
	    		if(propertiesInJSON.contains("nombre")) {
	    			paisEntity.setNombre(pais.getNombre() == null || pais.getNombre().trim().isEmpty() ? null : pais.getNombre().trim().toUpperCase());
	    		}
	    		
	    		// *** CAMPO << PREFIJO >>
	    		
	    		if(propertiesInJSON.contains("prefijo")) {
	    			paisEntity.setPrefijo(pais.getPrefijo() == null ? null : pais.getPrefijo());
	    		}
	    		
	    		paisFrmDto = new PaisFrmDto(paisEntity);
    		}
    		
			try {
				ValidatorUtil.validate(paisFrmDtoValidator, messageSource, paisFrmDto);
				return new PaisEdm( paisService.actualizar(paisFrmDto) );
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
    	}
    	
    	throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PAIS", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer paisID = Integer.valueOf( keyPredicateMap.get("id").getText() );

    	try {
			paisService.borrar(paisID);
		} catch (Exception e) {
			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
    	
    	return null;
	}
	
	@Override
	public DataSource getDataSource() {
		return this;
	}

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		Integer paisID = Integer.valueOf( keyPredicateMap.get("id").getText() );
		
		PaisEntity paisEntity = paisService.buscarPorID(paisID);
		PaisEdm paisEdm = paisEntity == null ? null : new PaisEdm(paisEntity);
		
		return paisEdm;
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(true)
			.setClazz(PaisEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
		
		List<PaisEntity> paisEntities = executeQueryListResult(query);
		List<PaisEdm> paisEdms = paisEntities.stream().map(entity -> { return new PaisEdm(entity); }).collect(Collectors.toList());
		
		return paisEdms;
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
