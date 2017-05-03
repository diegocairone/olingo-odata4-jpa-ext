package com.cairone.odataexample.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.ProvinciaFrmDto;
import com.cairone.odataexample.entities.PaisEntity;
import com.cairone.odataexample.entities.ProvinciaEntity;
import com.cairone.odataexample.entities.ProvinciaPKEntity;
import com.cairone.odataexample.repositories.PaisRepository;
import com.cairone.odataexample.repositories.ProvinciaRepository;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class ProvinciaService {

	@Autowired private PaisRepository paisRepository = null;
	@Autowired private ProvinciaRepository provinciaRepository = null;

	@Transactional(readOnly=true)
	public ProvinciaEntity buscarPorID(Integer paisID, Integer provinciaID) {
		
		ProvinciaEntity provinciaEntity = provinciaRepository.findOne(new ProvinciaPKEntity(paisID, provinciaID));
		return provinciaEntity;
	}

	@Transactional(readOnly=true)
	public List<ProvinciaEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList) {
		
		Iterable<ProvinciaEntity> provinciaEntities = orderByList == null || orderByList.size() == 0 ?
				provinciaRepository.findAll(expression) : provinciaRepository.findAll(expression, new Sort(orderByList));
				
		return (List<ProvinciaEntity>) provinciaEntities;
	}

	@Transactional(readOnly=true)
	public Page<ProvinciaEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList, int limit) {
		
		Page<ProvinciaEntity> pageProvinciaEntity = orderByList == null || orderByList.size() == 0 ?
				provinciaRepository.findAll(expression, new PageRequest(0, limit)) :
				provinciaRepository.findAll(expression, new PageRequest(0, limit, new Sort(orderByList)));
				
		return pageProvinciaEntity;
	}
	
	@Transactional
	public ProvinciaEntity nuevo(ProvinciaFrmDto provinciaFrmDto) throws Exception {
		
		PaisEntity paisEntity = paisRepository.findOne(provinciaFrmDto.getPaisID());
		
		if(paisEntity == null) {
			throw new Exception(String.format("NO SE ENCUENTRA EL PAIS CON ID %s", provinciaFrmDto.getPaisID()));
		}
		
		ProvinciaEntity provinciaEntity = new ProvinciaEntity();
		
		provinciaEntity.setId(provinciaFrmDto.getId());
		provinciaEntity.setPais(paisEntity);
		provinciaEntity.setNombre(provinciaFrmDto.getNombre());
		
		provinciaRepository.save(provinciaEntity);
		
		return provinciaEntity;
	}

	@Transactional
	public ProvinciaEntity actualizar(ProvinciaFrmDto provinciaFrmDto) throws Exception {
		
		if(provinciaFrmDto == null || provinciaFrmDto.getId() == null || provinciaFrmDto.getPaisID() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR LA PROVINCIA A ACTUALIZAR");
		}
		
		ProvinciaEntity provinciaEntity = provinciaRepository.findOne(new ProvinciaPKEntity(provinciaFrmDto.getPaisID(), provinciaFrmDto.getId()));
		
		if(provinciaEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA PROVINCIA CON ID [PAIS=%s,PROVINCIA=%s]", provinciaFrmDto.getPaisID(), provinciaFrmDto.getId()));
		}
		
		provinciaEntity.setNombre(provinciaFrmDto.getNombre());
		
		provinciaRepository.save(provinciaEntity);
		
		return provinciaEntity;
	}

	@Transactional
	public void borrar(Integer paisID, Integer provinciaID) throws Exception {
		
		ProvinciaEntity provinciaEntity = provinciaRepository.findOne(new ProvinciaPKEntity(paisID, provinciaID));
		
		if(provinciaEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA PROVINCIA CON ID [PAIS=%s,PROVINCIA=%s]", paisID, provinciaID));
		}
		
		provinciaRepository.delete(provinciaEntity);
	}
}
