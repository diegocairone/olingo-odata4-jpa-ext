package com.cairone.olingo.ext.demo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.olingo.ext.demo.dtos.PersonFrmDto;
import com.cairone.olingo.ext.demo.entities.FormEntity;
import com.cairone.olingo.ext.demo.entities.PersonEntity;
import com.cairone.olingo.ext.demo.entities.RegionEntity;
import com.cairone.olingo.ext.demo.entities.StateEntity;
import com.cairone.olingo.ext.demo.exceptions.ServiceException;
import com.cairone.olingo.ext.demo.repositories.FormRepository;
import com.cairone.olingo.ext.demo.repositories.PersonRepository;
import com.cairone.olingo.ext.demo.repositories.RegionRepository;
import com.cairone.olingo.ext.demo.repositories.StateRepository;

@Service
public class PersonService {

	@Autowired private FormRepository formRepository = null;
	@Autowired private PersonRepository personRepository = null;
	@Autowired private RegionRepository regionRepository = null;
	@Autowired private StateRepository stateRepository = null;
	
	public PersonRepository getPersonRepository() {
		return personRepository;
	}

	@Transactional(readOnly=true)
	public PersonEntity findOne(Integer id) throws ServiceException {
		
		if(id == null) throw new ServiceException(ServiceException.MISSING_DATA, "ENTITY ID CAN NOT BE NULL");
		Optional<PersonEntity> personEntityOptional = personRepository.findById(id);
		
		if(!personEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE FOUND AN ENTITY WITH ID %s", id));
		}
		
		return personEntityOptional.get();
	}

	@Transactional
	public PersonEntity save(PersonFrmDto personFrmDto) throws ServiceException {
		
		Integer regionId = personFrmDto.getRegion() == null ? null : personFrmDto.getRegion().getAsPrimitive();
		Optional<RegionEntity> regionEntityOptional = regionId == null ? Optional.empty() : regionRepository.findById(regionId);
		
		if(regionId != null && !regionEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, 
					String.format("COULD NOT BE FOUND A REGION ENTITY THAT CORRESPOND TO %s ENUM", personFrmDto.getRegion()));
		}
		
		String formId = personFrmDto.getFormId();
		Optional<FormEntity> formEntityOptional = formId == null ? null : formRepository.findById(formId);

		if(formId != null && !formEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, 
					String.format("COULD NOT BE FOUND A FORM ENTITY WITH ID %s", formId));
		}
		
		Integer stateId = personFrmDto.getStateId();
		Optional<StateEntity> stateEntityOptional = stateId == null ? Optional.empty() : stateRepository.findById(stateId);
		
		if(stateId != null && !stateEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, 
					String.format("COULD NOT BE FOUND A STATE ENTITY WITH ID %s", stateId));
		}
		
		PersonEntity personEntity = new PersonEntity(personFrmDto.getId(), personFrmDto.getName(),
				personFrmDto.getSurname(), personFrmDto.getGender(), regionEntityOptional.get(), formEntityOptional.get(),
				personFrmDto.getAddressStreet(), personFrmDto.getAddressNumber(), stateEntityOptional.get(),
				personFrmDto.getBirthDate());
		
		personRepository.save(personEntity);
		
		return personEntity;
	}

	@Transactional
	public void delete(PersonEntity personEntity) {
		personRepository.delete(personEntity);
	}
}
