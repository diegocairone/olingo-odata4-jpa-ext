package com.cairone.odataexample.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.PersonaFrmDto;
import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.entities.LocalidadPKEntity;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaFotoEntity;
import com.cairone.odataexample.entities.PersonaPKEntity;
import com.cairone.odataexample.entities.PersonaSectorEntity;
import com.cairone.odataexample.entities.PersonaSectorPKEntity;
import com.cairone.odataexample.entities.QPersonaEntity;
import com.cairone.odataexample.entities.QPersonaSectorEntity;
import com.cairone.odataexample.entities.SectorEntity;
import com.cairone.odataexample.entities.TipoDocumentoEntity;
import com.cairone.odataexample.repositories.LocalidadRepository;
import com.cairone.odataexample.repositories.PersonaFotoRepository;
import com.cairone.odataexample.repositories.PersonaRepository;
import com.cairone.odataexample.repositories.PersonaSectorRepository;
import com.cairone.odataexample.repositories.TipoDocumentoRepository;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class PersonaService {

	@Autowired private PersonaRepository personaRepository = null;
	@Autowired private PersonaSectorRepository personaSectorRepository = null;
	@Autowired private PersonaFotoRepository personaFotoRepository = null;
	@Autowired private LocalidadRepository localidadRepository = null;
	@Autowired private TipoDocumentoRepository tipoDocumentoRepository = null;

	@Transactional(readOnly=true)
	public PersonaEntity buscarPorId(Integer tipoDocumentoId, String numeroDocumento) {
		
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(tipoDocumentoId, numeroDocumento));
		return personaEntity;
	}
	
	@Transactional(readOnly=true)
	public PersonaEntity buscarPorFotoUUID(String uuid) {
		
		QPersonaEntity qPersona = QPersonaEntity.personaEntity;
		BooleanExpression exp = qPersona.fotoUUID.eq(uuid);
		
		PersonaEntity personaEntity = personaRepository.findOne(exp);
		
		return personaEntity;
	}
	
	@Transactional(readOnly=true)
	public List<PersonaEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList) {
		
		Iterable<PersonaEntity> personaEntities = orderByList == null || orderByList.size() == 0 ?
				personaRepository.findAll(expression) : personaRepository.findAll(expression, new Sort(orderByList));
		
		return (List<PersonaEntity>) personaEntities;
	}

	@Transactional(readOnly=true)
	public Page<PersonaEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList, int limit) {
		
		Page<PersonaEntity> pagePersonaEntity = orderByList == null || orderByList.size() == 0 ?
				personaRepository.findAll(expression, new PageRequest(0, limit)) :
				personaRepository.findAll(expression, new PageRequest(0, limit, new Sort(orderByList)));
				
		return pagePersonaEntity;
	}
	
	@Transactional(readOnly=true)
	public PersonaFotoEntity buscarFoto(String uuid) {
		
		PersonaFotoEntity personaFotoEntity = personaFotoRepository.findOne(uuid);
		return personaFotoEntity;
	}

	@Transactional(readOnly=true)
	public PersonaFotoEntity buscarFoto(PersonaEntity personaEntity) {
		return buscarFoto(personaEntity.getFotoUUID());
	}

	@Transactional
	public PersonaEntity nuevo(PersonaFrmDto personaFrmDto) throws Exception {

		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(personaFrmDto.getPaisId(), personaFrmDto.getProvinciaId(), personaFrmDto.getPaisId()));

		if(localidadEntity == null) {
			throw new Exception(String.format("NO SE ENCUENTRA LA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", personaFrmDto.getPaisId(), personaFrmDto.getProvinciaId(), personaFrmDto.getLocalidadId()));
		}
		
		TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoRepository.findOne(personaFrmDto.getTipoDocumentoId());

		if(tipoDocumentoEntity == null) {
			throw new Exception(String.format("NO SE ENCUENTRA UN TIPO DE DOCUMENTO CON ID %s", personaFrmDto.getTipoDocumentoId()));
		}

		PersonaEntity personaEntity = new PersonaEntity(tipoDocumentoEntity, personaFrmDto.getNumeroDocumento());
		
		personaEntity.setNombres(personaFrmDto.getNombres());
		personaEntity.setApellidos(personaFrmDto.getApellidos());
		personaEntity.setApodo(personaFrmDto.getApodo());
		personaEntity.setLocalidad(localidadEntity);
		personaEntity.setFechaAlta(LocalDate.now());
		personaEntity.setGenero(personaFrmDto.getGenero().toGeneroEnum());
		
		personaRepository.save(personaEntity);
		
		return personaEntity;
	}

	@Transactional
	public PersonaEntity actualizar(PersonaFrmDto personaFrmDto) throws Exception {

		if(personaFrmDto == null || personaFrmDto.getTipoDocumentoId() == null || personaFrmDto.getNumeroDocumento() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR LA PERSONA A ACTUALIZAR");
		}
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(personaFrmDto.getPaisId(), personaFrmDto.getProvinciaId(), personaFrmDto.getPaisId()));

		if(localidadEntity == null) {
			throw new Exception(String.format("NO SE ENCUENTRA LA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", personaFrmDto.getPaisId(), personaFrmDto.getProvinciaId(), personaFrmDto.getLocalidadId()));
		}
		
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(personaFrmDto.getTipoDocumentoId(), personaFrmDto.getNumeroDocumento()));
		
		if(personaEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA PERSONA CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", personaFrmDto.getTipoDocumentoId(), personaFrmDto.getNumeroDocumento()));
		}
		
		personaEntity.setNombres(personaFrmDto.getNombres());
		personaEntity.setApellidos(personaFrmDto.getApellidos());
		personaEntity.setApodo(personaFrmDto.getApodo());
		personaEntity.setLocalidad(localidadEntity);
		personaEntity.setGenero(personaFrmDto.getGenero().toGeneroEnum());
		
		personaRepository.save(personaEntity);
		
		return personaEntity;
	}

	@Transactional
	public void borrar(Integer tipoDocumentoID, String numeroDocumento) throws Exception {
		
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(tipoDocumentoID, numeroDocumento));

		if(personaEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA PERSONA CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", tipoDocumentoID, numeroDocumento));
		}

		
		personaRepository.delete(personaEntity);
	}

	@Transactional(readOnly=true)
	public Iterable<PersonaSectorEntity> buscarSectores(PersonaEntity personaEntity) {
		
		QPersonaSectorEntity q = QPersonaSectorEntity.personaSectorEntity;
		BooleanExpression exp = q.persona.eq(personaEntity);
		
		Iterable<PersonaSectorEntity> personaSectorEntities = personaSectorRepository.findAll(exp);
		
		return personaSectorEntities;
	}

	@Transactional(readOnly=true)
	public PersonaSectorEntity buscarIngresoEnSector(PersonaEntity personaEntity, SectorEntity sectorEntity) {
		
		QPersonaSectorEntity q = QPersonaSectorEntity.personaSectorEntity;
		BooleanExpression exp = q.pk.eq(new PersonaSectorPKEntity(personaEntity, sectorEntity));
		
		PersonaSectorEntity personaSectorEntity = personaSectorRepository.findOne(exp);
		
		return personaSectorEntity;
	}
	
	@Transactional
	public PersonaSectorEntity ingresarSector(PersonaEntity personaEntity, SectorEntity sectorEntity, LocalDate fechaIngreso) {
		
		PersonaSectorEntity personaSectorEntity = new PersonaSectorEntity();
		
		personaSectorEntity.setPersona(personaEntity);
		personaSectorEntity.setSector(sectorEntity);
		personaSectorEntity.setFechaIngreso(fechaIngreso);
		
		personaSectorRepository.save(personaSectorEntity);
		
		return personaSectorEntity;
	}

	@Transactional
	public void quitarDeSector(PersonaEntity personaEntity, SectorEntity sectorEntity) {
		
		PersonaSectorEntity personaSectorEntity = personaSectorRepository.findOne(new PersonaSectorPKEntity(personaEntity, sectorEntity));
		
		if(personaSectorEntity != null) {
			personaSectorRepository.delete(personaSectorEntity);
		}
	}
	
	@Transactional
	public PersonaFotoEntity nuevaFoto(byte[] foto) {
		
		PersonaFotoEntity personaFotoEntity = new PersonaFotoEntity(foto);
		personaFotoRepository.save(personaFotoEntity);
		
		return personaFotoEntity;
	}
	
	@Transactional
	public void asignarFoto(PersonaEntity personaEntity, PersonaFotoEntity personaFotoEntity) {
		personaEntity.setFotoUUID(personaFotoEntity.getUuid());
		personaRepository.save(personaEntity);
	}
	
	@Transactional
	public PersonaFotoEntity actualizarFoto(PersonaEntity personaEntity, byte[] foto) {

		String uuid = personaEntity.getFotoUUID();
		PersonaFotoEntity fotoEntity = uuid == null ? null : personaFotoRepository.findOne(uuid);
		
		if(fotoEntity == null) {
			fotoEntity = new PersonaFotoEntity(foto);
		} else {
			fotoEntity.setFoto(foto);
		}
		
		personaFotoRepository.save(fotoEntity);
		
		if(uuid == null) {
			personaEntity.setFotoUUID(uuid);
			personaRepository.save(personaEntity);
		}
		
		return fotoEntity;
	}

	@Transactional
	public void quitarFoto(PersonaEntity personaEntity) {
		
		if(personaEntity.getFotoUUID() != null) {
			PersonaFotoEntity fotoEntity = personaFotoRepository.findOne(personaEntity.getFotoUUID());
			personaEntity.setFotoUUID(null);
			
			personaRepository.save(personaEntity);
			personaFotoRepository.delete(fotoEntity);
		}
	}
	
	@Transactional
	public void quitarFoto(String uuid) {
		
		QPersonaEntity qPersona = QPersonaEntity.personaEntity;
		BooleanExpression exp = qPersona.fotoUUID.eq(uuid);
		
		PersonaEntity personaEntity = personaRepository.findOne(exp);
		
		if(personaEntity != null) {
			personaEntity.setFotoUUID(null);
			personaRepository.save(personaEntity);
		}
		
		PersonaFotoEntity personaFotoEntity = personaFotoRepository.findOne(uuid);
		personaFotoRepository.delete(personaFotoEntity);
	}
}
