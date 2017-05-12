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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.edm.resources.PersonaFotoEdm;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaFotoEntity;
import com.cairone.odataexample.services.PersonaService;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.DataSourceProvider;
import com.cairone.olingo.ext.jpa.interfaces.MediaDataSource;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class PersonaFotoDataSource implements DataSourceProvider, DataSource, MediaDataSource {
	
	private static final String ENTITY_SET_NAME = "PersonasFoto";
	
	@Autowired private PersonaService personaService = null;

	@Autowired
	private MessageSource messageSource = null;

	@Autowired
    private EntityManagerFactory entityManagerFactory;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public DataSource getDataSource() {
		return this;
	}
	
	@Override
	public byte[] findMediaResource(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
    	
    	PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoID, numeroDocumento);
    	PersonaFotoEntity personaFotoEntity = personaService.buscarFoto(personaEntity);
    	byte[] foto = personaFotoEntity == null ? null : personaFotoEntity.getFoto();
    	
    	return foto;
	}

	@Override
	public void updateMediaResource(Map<String, UriParameter> keyPredicateMap, byte[] binary) throws ODataApplicationException {
		
		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );

    	PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoID, numeroDocumento);
    	
    	personaService.actualizarFoto(personaEntity, binary);
	}

	@Override
	public Object create(Object entity) throws ODataException {
		
		if(entity instanceof PersonaFotoEdm) {
			
			PersonaFotoEdm personaFotoEdm = (PersonaFotoEdm) entity;
			
			Integer tipoDocumentoId = personaFotoEdm.getTipoDocumentoId();
			String numeroDocumento = personaFotoEdm.getNumeroDocumento();
			
			PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoId, numeroDocumento);
			PersonaFotoEntity personaFotoEntity = new PersonaFotoEntity(personaEntity);
			
			personaService.actualizarFoto(personaEntity, null);
			
			return new PersonaFotoEdm(personaFotoEntity);
		}
		
		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD FOTO PERSONA", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {
		throw new ODataApplicationException("Not implemented", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
    	
    	try {
    		PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoID, numeroDocumento);
			personaService.quitarFoto(personaEntity);
		} catch (Exception e) {
			throw new ODataApplicationException(
    			String.format("LA FOTO DE PERSONA CON ID (TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s) NO EXITE", tipoDocumentoID, numeroDocumento), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
    	
    	return null;
	}
	
	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
    	
    	PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoID, numeroDocumento);
    	PersonaFotoEntity personaFotoEntity = personaService.buscarFoto(personaEntity);
    	
    	PersonaFotoEdm personaFotoEdm = personaFotoEntity == null ? null : new PersonaFotoEdm(personaEntity);
    	
    	return personaFotoEdm;
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(PersonaFotoEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<PersonaFotoEntity> personaFotoEntities = executeQueryListResult(query);
		List<PersonaFotoEdm> personaFotoEdms = personaFotoEntities.stream().map(entity -> { return new PersonaFotoEdm(entity.getPersona()); }).collect(Collectors.toList());
		
		return personaFotoEdms;
	}

    @SuppressWarnings("unchecked")
	protected <T> List<T> executeQueryListResult(JPQLQuery jpaQuery) {

        EntityManager em = entityManagerFactory.createEntityManager();

        String queryString = jpaQuery.getQueryString();
    	
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
