package com.cairone.olingo.ext.demo.services;

import java.util.Optional;

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

	public PersonCheckLogRepository getPersonCheckLogRepository() {
		return personCheckLogRepository;
	}

	@Transactional(readOnly=true)
	public PersonCheckLogEntity findOne(Long id) throws ServiceException {
		
		if(id == null) throw new ServiceException(ServiceException.MISSING_DATA, "ENTITY ID CAN NOT BE NULL");
		Optional<PersonCheckLogEntity> personCheckLogEntityOptional = personCheckLogRepository.findById(id);
		
		if(!personCheckLogEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE FOUND AN ENTITY WITH ID %s", id));
		}
		
		return personCheckLogEntityOptional.get();
	}

	@Transactional
	public PersonCheckLogEntity save(PersonCheckLogFrmDto personCheckLogFrmDto) throws ServiceException {
		
		Integer personId = personCheckLogFrmDto.getPersonId();
		Optional<PersonEntity> personEntityOptional = personRepository.findById(personId);
		
		if(!personEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, 
					String.format("COULD NOT BE FOUND A PERSON ENTITY WITH ID %s", personId));
		}
				
		PersonCheckLogEntity personCheckLogEntity = 
				new PersonCheckLogEntity(personEntityOptional.get(), personCheckLogFrmDto.getCheckType(), personCheckLogFrmDto.getDatetime());
		
		personCheckLogRepository.save(personCheckLogEntity);
		
		return personCheckLogEntity;
	}

	@Transactional
	public void delete(PersonCheckLogEntity personCheckLogEntity) {
		personCheckLogRepository.delete(personCheckLogEntity);
	}
}
