package com.cairone.olingo.ext.demo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.olingo.ext.demo.dtos.CountryFrmDto;
import com.cairone.olingo.ext.demo.entities.CountryEntity;
import com.cairone.olingo.ext.demo.exceptions.ServiceException;
import com.cairone.olingo.ext.demo.repositories.CountryRepository;

@Service
public class CountryService {

	@Autowired private CountryRepository countryRepository = null;

	public CountryRepository getCountryRepository() {
		return countryRepository;
	}

	@Transactional(readOnly=true)
	public CountryEntity findOne(Integer id) throws ServiceException {
		
		if(id == null) throw new ServiceException(ServiceException.MISSING_DATA, "ENTITY ID CAN NOT BE NULL");
		Optional<CountryEntity> countryEntityOptional = countryRepository.findById(id);
		
		if(!countryEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE FOUND AN ENTITY WITH ID %s", id));
		}
		
		return countryEntityOptional.get();
	}

	@Transactional
	public CountryEntity save(CountryFrmDto countryFrmDto) throws ServiceException {
		
		CountryEntity countryEntity = new CountryEntity(countryFrmDto.getId(), countryFrmDto.getName());
		countryRepository.save(countryEntity);
		
		return countryEntity;
	}

	@Transactional
	public void delete(CountryEntity countryEntity) {
		countryRepository.delete(countryEntity);
	}
}
