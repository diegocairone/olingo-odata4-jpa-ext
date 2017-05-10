package com.cairone.odataexample.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.entities.QUsuarioPermisoEntity;
import com.cairone.odataexample.entities.UsuarioEntity;
import com.cairone.odataexample.entities.UsuarioPermisoEntity;
import com.cairone.odataexample.repositories.PermisoRepository;
import com.cairone.odataexample.repositories.UsuarioPermisoRepository;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class PermisoService {

	@Autowired private PermisoRepository permisoRepository = null;
	@Autowired private UsuarioPermisoRepository usuarioPermisoRepository = null;

	@Transactional(readOnly=true)
	public PermisoEntity buscarPorNombre(String nombre) {
		
		PermisoEntity permisoEntity = permisoRepository.findOne(nombre);
		return permisoEntity;
	}

	@Transactional(readOnly=true)
	public List<PermisoEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList) {
		
		Iterable<PermisoEntity> permisoEntities = orderByList == null || orderByList.size() == 0 ?
				permisoRepository.findAll(expression) : permisoRepository.findAll(expression, new Sort(orderByList));
				
		return (List<PermisoEntity>) permisoEntities;
	}

	@Transactional(readOnly=true)
	public Page<PermisoEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList, int limit) {
		
		Page<PermisoEntity> pagePermisoEntity = orderByList == null || orderByList.size() == 0 ?
				permisoRepository.findAll(expression, new PageRequest(0, limit)) :
				permisoRepository.findAll(expression, new PageRequest(0, limit, new Sort(orderByList)));
				
		return pagePermisoEntity;
	}
	
	@Transactional(readOnly=true)
	public List<UsuarioEntity> buscarUsuariosAsignados(PermisoEntity permisoEntity) {
		
		List<UsuarioEntity> usuarioEntities = new ArrayList<UsuarioEntity>();
		
		QUsuarioPermisoEntity q = QUsuarioPermisoEntity.usuarioPermisoEntity;
		BooleanExpression exp = q.permiso.eq(permisoEntity);
		
		Iterable<UsuarioPermisoEntity> iterable = usuarioPermisoRepository.findAll(exp);
		
		for(UsuarioPermisoEntity usuarioPermisoEntity : iterable) {
			usuarioEntities.add(usuarioPermisoEntity.getUsuario());
		}
		
		return usuarioEntities;
	}
}
