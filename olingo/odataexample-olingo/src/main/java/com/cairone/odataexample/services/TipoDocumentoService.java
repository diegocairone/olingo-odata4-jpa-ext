package com.cairone.odataexample.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.odataexample.dtos.TipoDocumentoFrmDto;
import com.cairone.odataexample.entities.TipoDocumentoEntity;
import com.cairone.odataexample.repositories.TipoDocumentoRepository;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class TipoDocumentoService {

	@Autowired private TipoDocumentoRepository tipoDocumentoRepository = null;
	
	@Transactional(readOnly=true)
	public TipoDocumentoEntity buscarPorID(Integer tipoDocumentoID) {
		
		TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoRepository.findOne(tipoDocumentoID);
		return tipoDocumentoEntity;
	}

	@Transactional(readOnly=true)
	public List<TipoDocumentoEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList) {
		
		Iterable<TipoDocumentoEntity> tipoDocumentoEntities = orderByList == null || orderByList.size() == 0 ?
				tipoDocumentoRepository.findAll(expression) : tipoDocumentoRepository.findAll(expression, new Sort(orderByList));
				
		return (List<TipoDocumentoEntity>) tipoDocumentoEntities;
	}

	@Transactional(readOnly=true)
	public Page<TipoDocumentoEntity> ejecutarConsulta(BooleanExpression expression, List<Sort.Order> orderByList, int limit) {
		
		Page<TipoDocumentoEntity> pageTipoDocumentoEntity = orderByList == null || orderByList.size() == 0 ?
				tipoDocumentoRepository.findAll(expression, new PageRequest(0, limit)) :
				tipoDocumentoRepository.findAll(expression, new PageRequest(0, limit, new Sort(orderByList)));
				
		return pageTipoDocumentoEntity;
	}
	
	@Transactional
	public TipoDocumentoEntity nuevo(TipoDocumentoFrmDto tipoDocumentoFrmDto) {
		
		TipoDocumentoEntity tipoDocumentoEntity = new TipoDocumentoEntity();
		
		tipoDocumentoEntity.setId(tipoDocumentoFrmDto.getId());
		tipoDocumentoEntity.setNombre(tipoDocumentoFrmDto.getNombre());
		tipoDocumentoEntity.setAbreviatura(tipoDocumentoFrmDto.getAbreviatura());
		
		tipoDocumentoRepository.save(tipoDocumentoEntity);
		
		return tipoDocumentoEntity;
	}

	@Transactional
	public TipoDocumentoEntity actualizar(TipoDocumentoFrmDto tipoDocumentoFrmDto) throws Exception {
		
		if(tipoDocumentoFrmDto == null || tipoDocumentoFrmDto.getId() == null) {
			throw new Exception("NO SE PUEDE IDENTIFICAR EL TIPO DE DOCUMENTO A ACTUALIZAR");
		}
		
		TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoRepository.findOne(tipoDocumentoFrmDto.getId());
		
		if(tipoDocumentoEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN TIPO DE DOCUMENTO CON ID %s", tipoDocumentoFrmDto.getId()));
		}
		
		tipoDocumentoEntity.setNombre(tipoDocumentoFrmDto.getNombre());
		tipoDocumentoEntity.setAbreviatura(tipoDocumentoFrmDto.getAbreviatura());
		
		tipoDocumentoRepository.save(tipoDocumentoEntity);
		
		return tipoDocumentoEntity;
	}

	@Transactional
	public void borrar(Integer tipoDocumentoID) throws Exception {
		
		TipoDocumentoEntity tipoDocumentoEntity = tipoDocumentoRepository.findOne(tipoDocumentoID);
		
		if(tipoDocumentoEntity == null) {
			throw new Exception(String.format("NO SE PUEDE ENCONTRAR UN TIPO DE DOCUMENTO CON ID %s", tipoDocumentoID));
		}
		
		tipoDocumentoRepository.delete(tipoDocumentoEntity);
	}
}
