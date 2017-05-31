package com.cairone.odataexample.datasources;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;

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
import com.cairone.olingo.ext.jpa.interfaces.MediaDataSource;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class PersonaFotoDataSource implements DataSource, MediaDataSource {
	
	private static final String ENTITY_SET_NAME = "PersonasFotos";
	
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
	public byte[] findMediaResource(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException {

		String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
    	PersonaFotoEntity personaFotoEntity = personaService.buscarFoto(uuid);

    	if(personaFotoEntity == null) {
    		throw new ODataApplicationException(String.format("NO EXISTE UNA FOTO DE PERSONA CON ID %s", uuid), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    	}
    	
    	byte[] foto = personaFotoEntity == null ? null : personaFotoEntity.getFoto();
    	
    	return foto;
	}

	@Override
	public Object createMediaResource(byte[] binary) throws ODataApplicationException {
		
		PersonaFotoEntity personaFotoEntity = personaService.nuevaFoto(binary);
		PersonaFotoEdm personaFotoEdm = new PersonaFotoEdm(personaFotoEntity.getUuid());
		
		return personaFotoEdm;
	}

	@Override
	public void updateMediaResource(Map<String, UriParameter> keyPredicateMap, byte[] binary) throws ODataApplicationException {
		
		String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
    	PersonaEntity personaEntity = personaService.buscarPorFotoUUID(uuid);
    	
    	personaService.actualizarFoto(personaEntity, binary);
	}

	@Override
	public Object create(Object entity) throws ODataException {
		throw new ODataApplicationException("Not implemented", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {
		
		if(entity instanceof PersonaFotoEdm) {
			
			PersonaFotoEdm personaFotoEdm = (PersonaFotoEdm) entity;
			
			String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
			PersonaFotoEntity personaFotoEntity = personaService.buscarFoto(uuid);
			
			if(personaFotoEntity == null) {
				throw new ODataApplicationException(String.format("NO EXISTE UNA FOTO DE PERSONA CO ID %s", uuid), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
			} else {
				Integer tipoDocumentoId = personaFotoEdm.getTipoDocumentoId();
				String numeroDocumento = personaFotoEdm.getNumeroDocumento();
				PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoId, numeroDocumento);
				
				if(personaEntity == null) {
					throw new ODataApplicationException(String.format("LA PERSONA CON ID (TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s) NO EXITE", tipoDocumentoId, numeroDocumento), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
				} else {
					personaService.asignarFoto(personaEntity, personaFotoEntity);
				}
				
				personaFotoEdm.setUuid(uuid);
				
				return personaFotoEdm;
			}
		}
		
		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD FOTO PERSONA", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
    	
    	try {
    		personaService.quitarFoto(uuid);
		} catch (Exception e) {
			throw new ODataApplicationException(
    			String.format("LA FOTO DE PERSONA CON ID %s NO EXITE", uuid), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
    	
    	return null;
	}
	
	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		String uuid = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("uuid").getText() );
    	PersonaFotoEntity personaFotoEntity = personaService.buscarFoto(uuid);
    	
    	if(personaFotoEntity == null) {
    		throw new ODataApplicationException(
        			String.format("LA FOTO DE PERSONA CON ID %s NO EXITE", uuid), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    	}
    	
    	PersonaEntity personaEntity = personaService.buscarPorFotoUUID(uuid);
    	
    	if(personaEntity == null) {
    		throw new ODataApplicationException(
        			String.format("LA FOTO %s NO EXITE ASOCIADA A NINGUNA PERSONA", uuid), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    	}
    	
    	PersonaFotoEdm personaFotoEdm = new PersonaFotoEdm(personaFotoEntity.getUuid());
    	personaFotoEdm.setTipoDocumentoId(personaEntity.getTipoDocumento().getId());
    	personaFotoEdm.setNumeroDocumento(personaEntity.getNumeroDocumento());
    	
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
	
		List<PersonaFotoEntity> personaFotoEntities = JPQLQuery.execute(entityManagerFactory, query);
		List<PersonaFotoEdm> personaFotoEdms = personaFotoEntities.stream().map(entity -> {
			
			PersonaEntity personaEntity = personaService.buscarPorFotoUUID(entity.getUuid());
			PersonaFotoEdm personaFotoEdm = new PersonaFotoEdm(entity.getUuid()); 
			
			if(personaEntity != null) {
				personaFotoEdm.setTipoDocumentoId(personaEntity.getTipoDocumento().getId());
				personaFotoEdm.setNumeroDocumento(personaEntity.getNumeroDocumento());
			}
			
			return personaFotoEdm; 
		}).collect(Collectors.toList());
		
		return personaFotoEdms;
	}
}
