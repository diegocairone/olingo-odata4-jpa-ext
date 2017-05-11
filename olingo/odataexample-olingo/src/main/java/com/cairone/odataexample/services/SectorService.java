package com.cairone.odataexample.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.SectorFrmDto;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaSectorEntity;
import com.cairone.odataexample.entities.PersonaSectorPKEntity;
import com.cairone.odataexample.entities.QPersonaSectorEntity;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.repositories.PersonaSectorRepository;
import com.cairone.odataexample.repositories.SectorRepository;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class SectorService {

	@Autowired private SectorRepository sectorRepository = null;
	@Autowired private PersonaSectorRepository personaSectorRepository = null;

	@Transactional(readOnly=true)
	public SectorEntity buscarPorID(Integer sectorID) {
		
		SectorEntity sectorEntity = sectorRepository.findOne(sectorID);
		return sectorEntity;
	}
	
	@Transactional(readOnly=true)
	public List<SectorEntity> buscarPorPersona(PersonaEntity personaEntity) {
		
		QPersonaSectorEntity q = QPersonaSectorEntity.personaSectorEntity;
		BooleanExpression exp = q.persona.eq(personaEntity);
		Iterable<PersonaSectorEntity> iterable = personaSectorRepository.findAll(exp);
		
		List<SectorEntity> sectorEntities = StreamSupport.stream(iterable.spliterator(), false)
				.map(PersonaSectorEntity::getSector)
				.collect(Collectors.toList());
		
		return sectorEntities;
	}

	@Transactional(readOnly=true)
	public List<SectorEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList) {
		
		Iterable<SectorEntity> sectorEntities = orderByList == null || orderByList.size() == 0 ?
				sectorRepository.findAll(expression) : sectorRepository.findAll(expression, new Sort(orderByList));
		
		return (List<SectorEntity>) sectorEntities;
	}

	@Transactional(readOnly=true)
	public Page<SectorEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList, int limit) {
		
		Page<SectorEntity> pageSectorEntity = orderByList == null || orderByList.size() == 0 ?
				sectorRepository.findAll(expression, new PageRequest(0, limit)) :
				sectorRepository.findAll(expression, new PageRequest(0, limit, new Sort(orderByList)));
				
		return pageSectorEntity;
	}
	
	@Transactional
	public SectorEntity nuevo(SectorFrmDto sectorFrmDto) {
		
		SectorEntity sectorEntity = new SectorEntity();
		
		sectorEntity.setId(sectorFrmDto.getId());
		sectorEntity.setNombre(sectorFrmDto.getNombre());
		
		sectorRepository.save(sectorEntity);
		
		return sectorEntity;
	}

	@Transactional
	public SectorEntity actualizar(SectorFrmDto sectorFrmDto) throws Exception {
		
		if(sectorFrmDto == null || sectorFrmDto.getId() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR EL SECTOR A ACTUALIZAR");
		}
		
		SectorEntity sectorEntity = sectorRepository.findOne(sectorFrmDto.getId());
		
		if(sectorEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN SECTOR CON ID %s", sectorFrmDto.getId()));
		}
		
		sectorEntity.setNombre(sectorFrmDto.getNombre());
		
		sectorRepository.save(sectorEntity);
		
		return sectorEntity;
	}

	@Transactional
	public void borrar(Integer sectorID) throws Exception {
		
		SectorEntity sectorEntity = sectorRepository.findOne(sectorID);
		
		if(sectorEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN SECTOR CON ID %s", sectorID));
		}
		
		sectorRepository.delete(sectorEntity);
	}
	
	@Transactional
	public PersonaSectorEntity agregarPersona(SectorEntity sectorEntity, PersonaEntity personaEntity) {
		
		PersonaSectorEntity personaSectorEntity = new PersonaSectorEntity();
		
		personaSectorEntity.setPersona(personaEntity);
		personaSectorEntity.setSector(sectorEntity);
		personaSectorEntity.setFechaIngreso(LocalDate.now());
		
		personaSectorRepository.save(personaSectorEntity);
		
		return personaSectorEntity;
	}

	@Transactional
	public void quitarPersona(SectorEntity sectorEntity, PersonaEntity personaEntity) {
		
		PersonaSectorPKEntity pk = new PersonaSectorPKEntity(personaEntity, sectorEntity);
		PersonaSectorEntity personaSectorEntity = personaSectorRepository.findOne(pk);
		
		personaSectorRepository.delete(personaSectorEntity);
	}
}
