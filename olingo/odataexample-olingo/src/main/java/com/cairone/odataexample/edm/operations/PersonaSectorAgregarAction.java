package com.cairone.odataexample.edm.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.edm.resources.PersonaSectorEdm;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaSectorEntity;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.services.PersonaService;
import com.cairone.odataexample.services.SectorService;
import com.cairone.olingo.ext.jpa.annotations.EdmAction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.google.common.base.CharMatcher;

@Component
@EdmAction(namespace = OdataExample.NAME_SPACE, name = "SectorAgregar", isBound = true, entitySetPath = "Personas") 
@EdmReturnType(type = "Collection(PersonaSector)")
public class PersonaSectorAgregarAction implements Operation<List<PersonaSectorEdm>> {

	@EdmParameter(nullable = false)
	private List<Integer> sectoresID = null;

	@Autowired private SectorService sectorService = null;
	@Autowired private PersonaService personaService = null; 

	@Override
	public List<PersonaSectorEdm> doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer tipoDocumentoId = Integer.valueOf(keyPredicateMap.get("tipoDocumentoId").getText());
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
				
		PersonaEntity personaEntity = personaService.buscarPorId(tipoDocumentoId, numeroDocumento);
		List<SectorEntity> sectorEntitiesQuitar = sectorService.buscarPorPersona(personaEntity)
				.stream()
				.filter(sectorEntity -> {
					return !sectoresID.contains(sectorEntity.getId());
				}).
				collect(Collectors.toList());
		
		sectorEntitiesQuitar.forEach(sectorEntity -> {
			sectorService.quitarPersona(sectorEntity, personaEntity);
		});
		
		List<PersonaSectorEdm> personaSectorEdms = new ArrayList<PersonaSectorEdm>();
		sectoresID.forEach(sectorID -> {
			
			SectorEntity sectorEntity = sectorService.buscarPorID(sectorID);
			PersonaSectorEntity personaSectorEntity = sectorService.agregarPersona(sectorEntity, personaEntity);
			
			PersonaSectorEdm personaSectorEdm = new PersonaSectorEdm(personaSectorEntity);
			personaSectorEdms.add(personaSectorEdm);
		});
		
		return personaSectorEdms;
	}
}
