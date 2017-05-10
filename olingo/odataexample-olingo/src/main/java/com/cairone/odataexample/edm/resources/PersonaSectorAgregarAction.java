package com.cairone.odataexample.edm.resources;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaPKEntity;
import com.cairone.odataexample.entities.PersonaSectorEntity;
import com.cairone.odataexample.entities.QPersonaSectorEntity;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.repositories.PersonaRepository;
import com.cairone.odataexample.repositories.PersonaSectorRepository;
import com.cairone.odataexample.repositories.SectorRepository;
import com.cairone.olingo.ext.jpa.annotations.EdmAction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;
import com.google.common.base.CharMatcher;
import com.mysema.query.types.expr.BooleanExpression;

@Component
@EdmAction(namespace = OdataExample.NAME_SPACE, name = "SectorAgregar", isBound = true, entitySetPath = "Personas") 
@EdmReturnType(type = "Collection(PersonaSector)")
public class PersonaSectorAgregarAction implements Operation<List<PersonaSectorEdm>> {

	@EdmParameter(nullable = false)
	private List<Integer> sectoresID = null;

	@Autowired private PersonaSectorRepository personaSectorRepository = null;
	@Autowired private PersonaRepository personaRepository = null;
	@Autowired private SectorRepository sectorRepository = null;
	
	@Override
	public List<PersonaSectorEdm> doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {

		Integer tipoDocumentoId = Integer.valueOf(keyPredicateMap.get("tipoDocumentoId").getText());
		String numeroDocumento = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("numeroDocumento").getText() );
				
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(tipoDocumentoId, numeroDocumento));
		
		QPersonaSectorEntity q = QPersonaSectorEntity.personaSectorEntity;
		BooleanExpression exp = q.persona.eq(personaEntity);
		Iterable<PersonaSectorEntity> iterable = personaSectorRepository.findAll(exp);
		
		List<PersonaSectorEntity> personaSectorEntitiesDelete = StreamSupport.stream(iterable.spliterator(), false)
			.filter(personaSectorEntity -> {
				return !sectoresID.contains(personaSectorEntity.getSector().getId());
			}).
			collect(Collectors.toList());
		
		personaSectorRepository.delete(personaSectorEntitiesDelete);
		
		List<PersonaSectorEdm> personaSectorEdms = new ArrayList<PersonaSectorEdm>();
		sectoresID.forEach(sectorID -> {
			
			SectorEntity sectorEntity = sectorRepository.findOne(sectorID);
			
			PersonaSectorEntity personaSectorEntity = new PersonaSectorEntity();
			
			personaSectorEntity.setPersona(personaEntity);
			personaSectorEntity.setSector(sectorEntity);
			personaSectorEntity.setFechaIngreso(LocalDate.now());
			
			personaSectorRepository.save(personaSectorEntity);
			
			PersonaSectorEdm personaSectorEdm = new PersonaSectorEdm(personaSectorEntity);
			personaSectorEdms.add(personaSectorEdm);
		});
		
		return personaSectorEdms;
	}
}
