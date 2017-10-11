package com.cairone.olingo.ext.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.olingo.ext.demo.dtos.PersonCheckLogFrmDto;
import com.cairone.olingo.ext.demo.entities.PersonCheckLogEntity;
import com.cairone.olingo.ext.demo.entities.PersonEntity;
import com.cairone.olingo.ext.demo.exceptions.ServiceException;
import com.cairone.olingo.ext.demo.repositories.PersonCheckLogRepository;
import com.cairone.olingo.ext.demo.repositories.PersonRepository;

@Service
public class PersonCheckLogService {

	@Autowired private PersonRepository personRepository = null;
	@Autowired private PersonCheckLogRepository personCheckLogRepository = null;

	@Transactional(readOnly=true)
	public PersonCheckLogEntity findOne(Long id) throws ServiceException {
		
		if(id == null) throw new ServiceException(ServiceException.MISSING_DATA, "ENTITY ID CAN NOT BE NULL");
		PersonCheckLogEntity personCheckLogEntity = personCheckLogRepository.findOne(id);
		
		if(personCheckLogEntity == null) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE FOUND AN ENTITY WITH ID %s", id));
		}
		
		return personCheckLogEntity;
	}

	@Transactional
	public PersonCheckLogEntity save(PersonCheckLogFrmDto personCheckLogFrmDto) throws ServiceException {
		
		Integer personId = personCheckLogFrmDto.getPersonId();
		PersonEntity personEntity = personRepository.findOne(personId);
		
		if(personEntity == null) {
			throw new ServiceException(ServiceException.NOT_FOUND, 
					String.format("COULD NOT BE FOUND A PERSON ENTITY WITH ID %s", personId));
		}
				
		PersonCheckLogEntity personCheckLogEntity = 
				new PersonCheckLogEntity(personEntity, personCheckLogFrmDto.getCheckType(), personCheckLogFrmDto.getDatetime());
		
		personCheckLogRepository.save(personCheckLogEntity);
		
		return personCheckLogEntity;
	}

	@Transactional
	public void delete(PersonCheckLogEntity personCheckLogEntity) {
		personCheckLogRepository.delete(personCheckLogEntity);
	}
}
