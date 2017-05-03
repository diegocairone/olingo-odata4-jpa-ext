package com.cairone.odataexample.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.LocalidadFrmDto;
import com.cairone.odataexample.entities.LocalidadEntity;
import com.cairone.odataexample.entities.LocalidadPKEntity;
import com.cairone.odataexample.entities.ProvinciaEntity;
import com.cairone.odataexample.entities.ProvinciaPKEntity;
import com.cairone.odataexample.repositories.LocalidadRepository;
import com.cairone.odataexample.repositories.PaisRepository;
import com.cairone.odataexample.repositories.ProvinciaRepository;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class LocalidadService {

	@Autowired private PaisRepository paisRepository = null;
	@Autowired private ProvinciaRepository provinciaRepository = null;
	@Autowired private LocalidadRepository localidadRepository = null;

	@Transactional(readOnly=true)
	public LocalidadEntity buscarPorID(Integer paisID, Integer provinciaID, Integer localidadID) {
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(paisID, provinciaID, localidadID));
		return localidadEntity;
	}

	@Transactional(readOnly=true)
	public List<LocalidadEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList) {
		
		Iterable<LocalidadEntity> localidadEntities = orderByList == null || orderByList.size() == 0 ?
				localidadRepository.findAll(expression) : localidadRepository.findAll(expression, new Sort(orderByList));
				
		return (List<LocalidadEntity>) localidadEntities;
	}

	@Transactional(readOnly=true)
	public Page<LocalidadEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList, int limit) {
		
		Page<LocalidadEntity> pageLocalidadEntity = orderByList == null || orderByList.size() == 0 ?
				localidadRepository.findAll(expression, new PageRequest(0, limit)) :
				localidadRepository.findAll(expression, new PageRequest(0, limit, new Sort(orderByList)));
				
		return pageLocalidadEntity;
	}
	
	@Transactional
	public LocalidadEntity nuevo(LocalidadFrmDto localidadFrmDto) throws Exception {
		
		ProvinciaEntity provinciaEntity = provinciaRepository.findOne(new ProvinciaPKEntity(localidadFrmDto.getPaisId(), localidadFrmDto.getProvinciaId()));

		if(provinciaEntity == null) {
			throw new Exception(String.format("NO SE ENCUENTRA LA PROVINCIA CON ID [PAIS=%s,PROVINCIA=%s]", localidadFrmDto.getPaisId(), localidadFrmDto.getProvinciaId()));
		}
		
		LocalidadEntity localidadEntity = new LocalidadEntity();
		
		localidadEntity.setId(localidadFrmDto.getLocalidadId());
		localidadEntity.setProvincia(provinciaEntity);
		localidadEntity.setNombre(localidadFrmDto.getNombre());
		localidadEntity.setCp(localidadFrmDto.getCp());
		localidadEntity.setPrefijo(localidadFrmDto.getPrefijo());
		
		localidadRepository.save(localidadEntity);
		
		return localidadEntity;
	}

	@Transactional
	public LocalidadEntity actualizar(LocalidadFrmDto provinciaFrmDto) throws Exception {
		
		if(provinciaFrmDto == null || provinciaFrmDto.getLocalidadId() == null || provinciaFrmDto.getProvinciaId() == null || provinciaFrmDto.getPaisId() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR LA LOCALIDAD A ACTUALIZAR");
		}
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(provinciaFrmDto.getPaisId(), provinciaFrmDto.getProvinciaId(), provinciaFrmDto.getLocalidadId()));
		
		if(localidadEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", provinciaFrmDto.getPaisId(), provinciaFrmDto.getProvinciaId(), provinciaFrmDto.getLocalidadId()));
		}
		
		localidadEntity.setNombre(provinciaFrmDto.getNombre());
		localidadEntity.setCp(provinciaFrmDto.getCp());
		localidadEntity.setPrefijo(provinciaFrmDto.getPrefijo());
		
		localidadRepository.save(localidadEntity);
		
		return localidadEntity;
	}

	@Transactional
	public void borrar(Integer paisID, Integer provinciaID, Integer localidadID) throws Exception {
		
		LocalidadEntity localidadEntity = localidadRepository.findOne(new LocalidadPKEntity(paisID, provinciaID, localidadID));
		
		if(localidadEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UNA LOCALIDAD CON ID [PAIS=%s,PROVINCIA=%s,LOCALIDAD=%s]", paisID, provinciaID, localidadID));
		}
		
		localidadRepository.delete(localidadEntity);
	}
}
