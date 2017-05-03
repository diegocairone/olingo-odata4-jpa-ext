package com.cairone.odataexample.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.UsuarioFrmDto;
import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaPKEntity;
import com.cairone.odataexample.entities.UsuarioEntity;
import com.cairone.odataexample.entities.UsuarioPKEntity;
import com.cairone.odataexample.entities.UsuarioPermisoEntity;
import com.cairone.odataexample.entities.UsuarioPermisoPKEntity;
import com.cairone.odataexample.repositories.PersonaRepository;
import com.cairone.odataexample.repositories.UsuarioPermisoRepository;
import com.cairone.odataexample.repositories.UsuarioRepository;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class UsuarioService {

	@Autowired private UsuarioRepository usuarioRepository = null;
	@Autowired private UsuarioPermisoRepository usuarioPermisoRepository = null;
	@Autowired private PersonaRepository personaRepository = null;

	@Transactional(readOnly=true)
	public UsuarioEntity buscarPorId(Integer tipoDocumentoId, String numeroDocumento) {
		
		UsuarioEntity usuarioEntity = usuarioRepository.findOne(new UsuarioPKEntity(tipoDocumentoId, numeroDocumento));
		return usuarioEntity;
	}

	@Transactional(readOnly=true)
	public UsuarioEntity buscarPorPersona(PersonaEntity personaEntity) {
		
		UsuarioEntity usuarioEntity = usuarioRepository.findOne(
				new UsuarioPKEntity(
						personaEntity.getTipoDocumento().getId(), 
						personaEntity.getNumeroDocumento()));
		
		return usuarioEntity;
	}
	
	@Transactional(readOnly=true)
	public List<UsuarioEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList) {
		
		Iterable<UsuarioEntity> usuarioEntities = orderByList == null || orderByList.size() == 0 ?
				usuarioRepository.findAll(expression) : usuarioRepository.findAll(expression, new Sort(orderByList));
				
		return (List<UsuarioEntity>) usuarioEntities;
	}

	@Transactional(readOnly=true)
	public Page<UsuarioEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList, int limit) {
		
		Page<UsuarioEntity> pageUsuarioEntity = orderByList == null || orderByList.size() == 0 ?
				usuarioRepository.findAll(expression, new PageRequest(0, limit)) :
				usuarioRepository.findAll(expression, new PageRequest(0, limit, new Sort(orderByList)));
				
		return pageUsuarioEntity;
	}

	@Transactional
	public UsuarioEntity nuevo(UsuarioFrmDto usuarioFrmDto) throws Exception {
		
		PersonaEntity personaEntity = personaRepository.findOne(new PersonaPKEntity(usuarioFrmDto.getTipoDocumentoId(), usuarioFrmDto.getNumeroDocumento()));

		if(personaEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA PERSONA CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", usuarioFrmDto.getTipoDocumentoId(), usuarioFrmDto.getNumeroDocumento()));
		}
		
		UsuarioEntity usuarioEntity = new UsuarioEntity(personaEntity);
		
		usuarioEntity.setNombreUsuario(usuarioFrmDto.getNombreUsuario());
		usuarioEntity.setClave("DEBE-DEFINIRSE");
		usuarioEntity.setFechaAlta(LocalDate.now());
		usuarioEntity.setCuentaVencida(usuarioFrmDto.getCuentaVencida());
		usuarioEntity.setClaveVencida(usuarioFrmDto.getClaveVencida());
		usuarioEntity.setCuentaBloqueada(usuarioFrmDto.getCuentaBloqueada());
		usuarioEntity.setUsuarioHabilitado(usuarioFrmDto.getUsuarioHabilitado());
		
		usuarioRepository.save(usuarioEntity);
		
		return usuarioEntity;
	}


	@Transactional
	public UsuarioEntity actualizar(UsuarioFrmDto usuarioFrmDto) throws Exception {

		if(usuarioFrmDto == null || usuarioFrmDto.getTipoDocumentoId() == null || usuarioFrmDto.getNumeroDocumento() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR EL USUARIO A ACTUALIZAR");
		}

		UsuarioEntity usuarioEntity = usuarioRepository.findOne(new UsuarioPKEntity(usuarioFrmDto.getTipoDocumentoId(), usuarioFrmDto.getNumeroDocumento()));

		if(usuarioEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN USUARIO CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", usuarioFrmDto.getTipoDocumentoId(), usuarioFrmDto.getNumeroDocumento()));
		}
		
		usuarioEntity.setNombreUsuario(usuarioFrmDto.getNombreUsuario());
		usuarioEntity.setCuentaVencida(usuarioFrmDto.getCuentaVencida());
		usuarioEntity.setClaveVencida(usuarioFrmDto.getClaveVencida());
		usuarioEntity.setCuentaBloqueada(usuarioFrmDto.getCuentaBloqueada());
		usuarioEntity.setUsuarioHabilitado(usuarioFrmDto.getUsuarioHabilitado());
		
		usuarioRepository.save(usuarioEntity);
		
		return usuarioEntity;
	}

	@Transactional
	public void borrar(Integer tipoDocumentoID, String numeroDocumento) throws Exception {
		
		UsuarioEntity usuarioEntity = usuarioRepository.findOne(new UsuarioPKEntity(tipoDocumentoID, numeroDocumento));

		if(usuarioEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN USUARIO CON ID [TIPODOCUMENTO=%s,NUMERODOCUMENTO=%s]", tipoDocumentoID, numeroDocumento));
		}
		
		usuarioRepository.delete(usuarioEntity);
	}

	@Transactional(readOnly=true)
	public UsuarioPermisoEntity buscarUnPermisoAsignado(UsuarioEntity usuarioEntity, PermisoEntity permisoEntity) {
		
		UsuarioPermisoEntity usuarioPermisoEntity = usuarioPermisoRepository.findOne(new UsuarioPermisoPKEntity(usuarioEntity, permisoEntity));
		return usuarioPermisoEntity;
	}
	
	@Transactional
	public UsuarioPermisoEntity asignarPermiso(UsuarioEntity usuarioEntity, PermisoEntity permisoEntity) {
		
		UsuarioPermisoEntity usuarioPermisoEntity = new UsuarioPermisoEntity(usuarioEntity, permisoEntity);
		usuarioPermisoRepository.save(usuarioPermisoEntity);
		
		return usuarioPermisoEntity;
	}

	@Transactional
	public void quitarPermiso(UsuarioEntity usuarioEntity, PermisoEntity permisoEntity) {
		
		UsuarioPermisoEntity usuarioPermisoEntity = usuarioPermisoRepository.findOne(new UsuarioPermisoPKEntity(usuarioEntity, permisoEntity));
		
		if(usuarioPermisoEntity != null) {
			usuarioPermisoRepository.delete(usuarioPermisoEntity);
		}
	}
}
