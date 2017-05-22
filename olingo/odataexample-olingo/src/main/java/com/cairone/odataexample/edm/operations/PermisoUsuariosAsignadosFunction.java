package com.cairone.odataexample.edm.operations;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.edm.resources.UsuarioEdm;
import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.entities.UsuarioEntity;
import com.cairone.odataexample.services.PermisoService;
import com.cairone.olingo.ext.jpa.annotations.EdmFunction;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.google.common.base.CharMatcher;

@Component
@EdmFunction(namespace = OdataExample.NAME_SPACE, name = "UsuariosAsignados", isBound = true, entitySetPath = "Permisos")
@EdmReturnType(type = "Collection(Usuario)")
public class PermisoUsuariosAsignadosFunction implements Operation<List<UsuarioEdm>> {

	@Autowired PermisoService permisoService = null;
	
	@Override
	public List<UsuarioEdm> doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		UriParameter key = keyPredicateMap.get("id");
		String permisoID = CharMatcher.is('\'').trimFrom(key.getText());
		
		PermisoEntity permisoEntity = permisoService.buscarPorNombre(permisoID);
		
		if(permisoEntity == null) {
			throw new ODataApplicationException(String.format("NO EXISTE UN PERMISO CON ID %s", permisoID), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
		
		List<UsuarioEntity> usuarioEntities = permisoService.buscarUsuariosAsignados(permisoEntity);
		List<UsuarioEdm> usuarioEdms = UsuarioEdm.crearLista(usuarioEntities);
		
		return usuarioEdms;
	}
}
