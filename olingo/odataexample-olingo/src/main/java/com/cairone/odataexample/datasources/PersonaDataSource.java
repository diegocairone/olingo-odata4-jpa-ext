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

import com.cairone.odataexample.dtos.PersonaFrmDto;
import com.cairone.odataexample.dtos.validators.PersonaFrmDtoValidator;
import com.cairone.odataexample.edm.resources.PersonaEdm;
import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.services.LocalidadService;
import com.cairone.odataexample.services.PersonaService;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class PersonaDataSource implements DataSource {

	private static final String ENTITY_SET_NAME = "Personas";
	
	@Autowired private PersonaService personaService = null;
	@Autowired private LocalidadService localidadService = null;
	@Autowired private PersonaFrmDtoValidator personaFrmDtoValidator = null;

	@Autowired
	private MessageSource messageSource = null;

	@Autowired
    private EntityManagerFactory entityManagerFactory;

	@Override
	public Object create(Object entity) throws ODataException {

		if(entity instanceof PersonaEdm) {
			
			PersonaEdm personaEdm = (PersonaEdm) entity;
			PersonaFrmDto personaFrmDto = new PersonaFrmDto(personaEdm);

			try {
				ValidatorUtil.validate(personaFrmDtoValidator, messageSource, personaFrmDto);
				PersonaEntity personaEntity = personaService.nuevo(personaFrmDto);
				return new PersonaEdm(personaEntity);
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		}
		
		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PERSONA", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {

    	if(entity instanceof PersonaEdm) {
    		
    		PersonaEdm persona = (PersonaEdm) entity;
    		PersonaFrmDto personaFrmDto;
    		
        	Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
        	String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
        	
    		if(isPut) {
    			personaFrmDto = new PersonaFrmDto(persona);
    			personaFrmDto.setTipoDocumentoId(tipoDocumentoID);
    			personaFrmDto.setNumeroDocumento(numeroDocumento);
    		} else {
	    		PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoID, numeroDocumento);
	    		
	    		if(personaEntity == null) {
	    			throw new ODataApplicationException(
	    				String.format("LA PERSONA CON ID (TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s) NO EXITE", tipoDocumentoID, numeroDocumento), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    		}
	    		
	    		// *** CAMPO << NOMBRE >>
	    		
	    		if(propertiesInJSON.contains("nombres")) {
	    			personaEntity.setNombres(persona.getNombres() == null || persona.getNombres().trim().isEmpty() ? null : persona.getNombres().trim().toUpperCase());
	    		}

	    		// *** CAMPO << APELLIDOS >>
	    		
	    		if(propertiesInJSON.contains("apellidos")) {
	    			personaEntity.setApellidos(persona.getApellidos() == null || persona.getApellidos().trim().isEmpty() ? null : persona.getApellidos().trim().toUpperCase());
	    		}

	    		// *** CAMPO << APODO >>
	    		
	    		if(propertiesInJSON.contains("apodo")) {
	    			personaEntity.setApodo(persona.getApodo() == null || persona.getApodo().trim().isEmpty() ? null : persona.getApodo().trim().toUpperCase());
	    		}

	    		// *** CAMPO << LOCALIDAD >>
	    		
	    		if(propertiesInJSON.contains("localidad")) {
	    			
	    			Integer paisID = persona.getLocalidad().getPaisId();
	    			Integer provinciaID = persona.getLocalidad().getProvinciaId();
	    			Integer localidadID = persona.getLocalidad().getLocalidadId();
	    			
	    			LocalidadEntity localidadEntity = localidadService.buscarPorID(paisID, provinciaID, localidadID);
	    			
	    			if(localidadEntity == null) {
	    				throw new ODataApplicationException(
	    						String.format("No se encuentra la localidad con ID (PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s)", paisID, provinciaID, localidadID), 
	    						HttpStatusCode.NOT_FOUND.getStatusCode(), 
	    						Locale.ENGLISH);
	    			}
	    			
	    			personaEntity.setLocalidad(localidadEntity);
	    		}
	    		
	    		// *** CAMPO << FECHA ALTA >>
	    		
	    		if(propertiesInJSON.contains("fechaAlta")) {
	    			personaEntity.setFechaAlta(persona.getFechaAlta());
	    		}

	    		// *** CAMPO << FECHA GENERO >>
	    		
	    		if(propertiesInJSON.contains("genero")) {
	    			personaEntity.setGenero(persona.getGenero().toGeneroEnum());
	    		}
	    		
	    		personaFrmDto = new PersonaFrmDto(personaEntity);
    		}
    		
			try {
				ValidatorUtil.validate(personaFrmDtoValidator, messageSource, personaFrmDto);
				return new PersonaEdm( personaService.actualizar(personaFrmDto) );
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
    	}
    	
    	throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PERSONA", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
    	
    	try {
			personaService.borrar(tipoDocumentoID, numeroDocumento);
		} catch (Exception e) {
			throw new ODataApplicationException(
    			String.format("LA PERSONA CON ID (TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s) NO EXITE", tipoDocumentoID, numeroDocumento), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
    	
    	return null;
	}

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
    	
    	PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoID, numeroDocumento);
    	PersonaEdm personaEdm = personaEntity == null ? null : new PersonaEdm(personaEntity);
    	
    	return personaEdm;
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(false)
			.setClazz(PersonaEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
	
		List<PersonaEntity> personaEntities = JPQLQuery.execute(entityManagerFactory, query);
		List<PersonaEdm> personaEdms = personaEntities.stream().map(entity -> { return new PersonaEdm(entity); }).collect(Collectors.toList());
		
		return personaEdms;
	}
}
