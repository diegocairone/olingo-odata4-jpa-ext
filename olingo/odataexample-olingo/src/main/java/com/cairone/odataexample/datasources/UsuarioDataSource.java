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

import com.cairone.odataexample.dtos.UsuarioFrmDto;
import com.cairone.odataexample.dtos.validators.UsuarioFrmDtoValidator;
import com.cairone.odataexample.edm.resources.UsuarioEdm;
import com.cairone.odataexample.entities.UsuarioEntity;
import com.cairone.odataexample.services.UsuarioService;
import com.cairone.odataexample.utils.SQLExceptionParser;
import com.cairone.odataexample.utils.ValidatorUtil;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class UsuarioDataSource implements DataSource {

	private static final String ENTITY_SET_NAME = "Usuarios";
	
	@Autowired private UsuarioService usuarioService = null;
	@Autowired private UsuarioFrmDtoValidator usuarioFrmDtoValidator = null;

	@Autowired
    private EntityManagerFactory entityManagerFactory;
	
	@Autowired
	private MessageSource messageSource = null;

	@Override
	public Object create(Object entity) throws ODataException {

		if(entity instanceof UsuarioEdm) {
			
			UsuarioEdm usuarioEdm = (UsuarioEdm) entity;
			UsuarioFrmDto usuarioFrmDto = new UsuarioFrmDto(usuarioEdm);

			try {
				ValidatorUtil.validate(usuarioFrmDtoValidator, messageSource, usuarioFrmDto);
				UsuarioEntity usuarioEntity = usuarioService.nuevo(usuarioFrmDto);
				return new UsuarioEdm(usuarioEntity);
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
		}
		
		throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PERSONA", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {

    	if(entity instanceof UsuarioEdm) {
    		
    		UsuarioEdm usuario = (UsuarioEdm) entity;
    		UsuarioFrmDto usuarioFrmDto;
    		
        	Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
        	String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
        	
    		if(isPut) {
    			usuarioFrmDto = new UsuarioFrmDto(usuario);
    			usuarioFrmDto.setTipoDocumentoId(tipoDocumentoID);
    			usuarioFrmDto.setNumeroDocumento(numeroDocumento);
    		} else {
    			UsuarioEntity usuarioEntity = usuarioService.buscarPorId(tipoDocumentoID, numeroDocumento);
	    		
	    		if(usuarioEntity == null) {
	    			throw new ODataApplicationException(
	    				String.format("EL USUARIO CON ID (TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s) NO EXITE", tipoDocumentoID, numeroDocumento), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    		}
	    		
	    		// *** CAMPO << NOMBRE >>
	    		
	    		if(propertiesInJSON.contains("nombreUsuario")) {
	    			usuarioEntity.setNombreUsuario(usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty() ? null : usuario.getNombreUsuario().trim().toLowerCase());
	    		}

	    		// *** CAMPO << FECHA ALTA >>
	    		
	    		if(propertiesInJSON.contains("fechaAlta")) {
	    			usuarioEntity.setFechaAlta(usuario.getFechaAlta());
	    		}
	    		
	    		// *** CAMPO << CUENTA VENCIDA >>
	    		
	    		if(propertiesInJSON.contains("cuentaVencida")) {
	    			usuarioEntity.setCuentaVencida(usuario.getCuentaVencida());
	    		}

	    		// *** CAMPO << CLAVE VENCIDA >>
	    		
	    		if(propertiesInJSON.contains("claveVencida")) {
	    			usuarioEntity.setClaveVencida(usuario.getClaveVencida());
	    		}

	    		// *** CAMPO << CUENTA BLOQUEADA >>
	    		
	    		if(propertiesInJSON.contains("cuentaBloqueada")) {
	    			usuarioEntity.setCuentaBloqueada(usuario.getCuentaBloqueada());
	    		}

	    		// *** CAMPO << USUARIO HABILITADO >>
	    		
	    		if(propertiesInJSON.contains("usuarioHabilitado")) {
	    			usuarioEntity.setUsuarioHabilitado(usuario.getUsuarioHabilitado());
	    		}
	    		
	    		usuarioFrmDto = new UsuarioFrmDto(usuarioEntity);
    		}
    		
			try {
				ValidatorUtil.validate(usuarioFrmDtoValidator, messageSource, usuarioFrmDto);
				return new UsuarioEdm( usuarioService.actualizar(usuarioFrmDto) );
			} catch (Exception e) {
				String message = SQLExceptionParser.parse(e);
				throw new ODataApplicationException(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
			}
    	}
    	
    	throw new ODataApplicationException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD USUARIO", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer tipoDocumentoID = Integer.valueOf( keyPredicateMap.get("tipoDocumentoId").getText() );
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
    	
    	try {
			usuarioService.borrar(tipoDocumentoID, numeroDocumento);
		} catch (Exception e) {
			throw new ODataApplicationException(
    			String.format("EL USUARIO CON ID (TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s) NO EXITE", tipoDocumentoID, numeroDocumento), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
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
    	
		UsuarioEntity usuarioEntity = usuarioService.buscarPorId(tipoDocumentoID, numeroDocumento);
		UsuarioEdm usuarioEdm = usuarioEntity == null ? null : new UsuarioEdm(usuarioEntity);
		
		return usuarioEdm;
	}
	
	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(true)
			.setClazz(UsuarioEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();
		
		List<UsuarioEntity> usuarioEntities = JPQLQuery.execute(entityManagerFactory, query);
		List<UsuarioEdm> usuarioEdms = usuarioEntities.stream().map(entity -> { return new UsuarioEdm(entity); }).collect(Collectors.toList());
		
		return usuarioEdms;
	}
}
