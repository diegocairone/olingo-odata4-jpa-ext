package com.cairone.olingo.ext.demo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.olingo.ext.demo.dtos.FormFrmDto;
import com.cairone.olingo.ext.demo.entities.FormEntity;
import com.cairone.olingo.ext.demo.exceptions.ServiceException;
import com.cairone.olingo.ext.demo.repositories.FormRepository;

@Service
public class FormService {

	@Autowired private FormRepository formRepository = null;

	@Transactional(readOnly=true)
	public FormEntity findOne(String id) throws ServiceException {
		
		if(id == null) throw new ServiceException(ServiceException.MISSING_DATA, "ENTITY ID CAN NOT BE NULL");
		Optional<FormEntity> formEntityOptional = formRepository.findById(id);
		
		if(!formEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE FOUND AN ENTITY WITH ID %s", id));
		}
		
		return formEntityOptional.get();
	}

	@Transactional
	public FormEntity save(FormFrmDto formFrmDto) throws ServiceException {
		
		FormEntity formEntity = new FormEntity(formFrmDto.getId(), formFrmDto.getName());
		formRepository.save(formEntity);
		
		return formEntity;
	}

	@Transactional
	public void delete(FormEntity formEntity) {
		formRepository.delete(formEntity);
	}
}
