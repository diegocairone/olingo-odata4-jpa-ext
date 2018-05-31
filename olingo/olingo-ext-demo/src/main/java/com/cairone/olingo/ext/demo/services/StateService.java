package com.cairone.olingo.ext.demo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.olingo.ext.demo.dtos.StateFrmDto;
import com.cairone.olingo.ext.demo.entities.CountryEntity;
import com.cairone.olingo.ext.demo.entities.QStateEntity;
import com.cairone.olingo.ext.demo.entities.StateEntity;
import com.cairone.olingo.ext.demo.exceptions.ServiceException;
import com.cairone.olingo.ext.demo.repositories.CountryRepository;
import com.cairone.olingo.ext.demo.repositories.StateRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class StateService {

	@Autowired private CountryRepository countryRepository = null;
	@Autowired private StateRepository stateRepository = null;

	public StateRepository getStateRepository() {
		return stateRepository;
	}

	@Transactional(readOnly=true)
	public StateEntity findOne(Integer id) throws ServiceException {
		
		if(id == null) throw new ServiceException(ServiceException.MISSING_DATA, "ENTITY ID CAN NOT BE NULL");
		Optional<StateEntity> stateEntityOptional = stateRepository.findById(id);
		
		if(!stateEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE FOUND AN ENTITY WITH ID %s", id));
		}
		
		return stateEntityOptional.get();
	}
	
	@Transactional(readOnly=true)
	public List<StateEntity> findByCountry(CountryEntity countryEntity) throws ServiceException {
		QStateEntity q = QStateEntity.stateEntity;
		BooleanExpression exp = q.country.eq(countryEntity);
		return (List<StateEntity>) stateRepository.findAll(exp);
	}

	@Transactional
	public StateEntity save(StateFrmDto stateFrmDto) throws ServiceException {
		
		Integer countryID = stateFrmDto.getCountryId();
		Optional<CountryEntity> countryEntityOptional = countryRepository.findById(countryID);
		
		if(!countryEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE A COUNTRY ENTITY WITH ID %s", countryID));
		}
		
		CountryEntity countryEntity = countryEntityOptional.get();
		StateEntity stateEntity = new StateEntity(stateFrmDto.getId(), stateFrmDto.getName(), countryEntity);
		stateRepository.save(stateEntity);
		
		return stateEntity;
	}

	@Transactional
	public void delete(StateEntity stateEntity) {
		stateRepository.delete(stateEntity);
	}
}
