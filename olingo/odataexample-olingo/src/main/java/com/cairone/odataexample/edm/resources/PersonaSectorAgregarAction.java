package com.cairone.odataexample.edm.resources;

import java.time.LocalDate;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaPKEntity;
import com.cairone.odataexample.entities.PersonaSectorEntity;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.repositories.PersonaRepository;
import com.cairone.odataexample.repositories.PersonaSectorRepository;
import com.cairone.odataexample.repositories.SectorRepository;
import com.cairone.olingo.ext.jpa.annotations.EdmAction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.google.common.base.CharMatcher;

@Component
@EdmAction(namespace = OdataExample.NAME_SPACE, name = "SectorAgregar", isBound = true, entitySetPath = "Personas") 
@EdmReturnType(type = "PersonaSector")
public class PersonaSectorAgregarAction implements Operation<PersonaSectorEdm> {
/*
	@EdmParameter(nullable = false)
	private Integer tipoDocumentoId = null;
	
	@EdmParameter(nullable = false)
	private String numeroDocumento = null;
	*/
	@EdmParameter(nullable = false)
	private Integer sectorId = null;

	@EdmParameter(nullable = false)
	private LocalDate fechaIngreso = null;

	@Autowired private PersonaSectorRepository personaSectorRepository = null;
	@Autowired private PersonaRepository personaRepository = null;
	@Autowired private SectorRepository sectorRepository = null;
	
	@Override
	public PersonaSectorEdm doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer tipoDocumentoId = Integer.valueOf(keyPredicateMap.get("tipoDocumentoId").getText());
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
				
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(tipoDocumentoId, numeroDocumento));
		SectorEntity sectorEntity = sectorRepository.findOne(sectorId);
		
		PersonaSectorEntity personaSectorEntity = new PersonaSectorEntity();
		
		personaSectorEntity.setPersona(personaEntity);
		personaSectorEntity.setSector(sectorEntity);
		personaSectorEntity.setFechaIngreso(LocalDate.now());
		
		personaSectorRepository.save(personaSectorEntity);
		
		PersonaSectorEdm personaSectorEdm = new PersonaSectorEdm(personaSectorEntity);
		
		return personaSectorEdm;
	}
}
