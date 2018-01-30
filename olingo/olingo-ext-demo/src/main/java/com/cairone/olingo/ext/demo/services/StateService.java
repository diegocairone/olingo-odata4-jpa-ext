package com.cairone.olingo.ext.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.olingo.ext.demo.dtos.StateFrmDto;
import com.cairone.olingo.ext.demo.entities.CountryEntity;
import com.cairone.olingo.ext.demo.entities.StateEntity;
import com.cairone.olingo.ext.demo.exceptions.ServiceException;
import com.cairone.olingo.ext.demo.repositories.CountryRepository;
import com.cairone.olingo.ext.demo.repositories.StateRepository;

@Service
public class StateService {

	@Autowired private CountryRepository countryRepository = null;
	@Autowired private StateRepository stateRepository = null;

	@Transactional(readOnly=true)
	public StateEntity findOne(Integer id) throws ServiceException {
		
		if(id == null) throw new ServiceException(ServiceException.MISSING_DATA, "ENTITY ID CAN NOT BE NULL");
		StateEntity stateEntity = stateRepository.findOne(id);
		
		if(stateEntity == null) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE FOUND AN ENTITY WITH ID %s", id));
		}
		
		return stateEntity;
	}

	@Transactional
	public StateEntity save(StateFrmDto stateFrmDto) throws ServiceException {
		
		Integer countryID = stateFrmDto.getCountryId();
		CountryEntity countryEntity = countryRepository.findOne(countryID);
		
		if(countryEntity == null) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE A COUNTRY ENTITY WITH ID %s", countryID));
		}
		
		StateEntity stateEntity = new StateEntity(stateFrmDto.getId(), stateFrmDto.getName(), countryEntity);
		stateRepository.save(stateEntity);
		
		return stateEntity;
	}

	@Transactional
	public void delete(StateEntity stateEntity) {
		stateRepository.delete(stateEntity);
	}
}
