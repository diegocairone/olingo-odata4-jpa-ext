package com.cairone.olingo.ext.demo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.olingo.ext.demo.dtos.UserFrmDto;
import com.cairone.olingo.ext.demo.entities.PersonEntity;
import com.cairone.olingo.ext.demo.entities.UserEntity;
import com.cairone.olingo.ext.demo.exceptions.ServiceException;
import com.cairone.olingo.ext.demo.repositories.PersonRepository;
import com.cairone.olingo.ext.demo.repositories.UserRepository;

@Service
public class UserService {

	@Autowired private PersonRepository personRepository = null;
	@Autowired private UserRepository userRepository = null;
	
	public UserRepository getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly=true)
	public UserEntity findOne(Integer id) throws ServiceException {
		
		if(id == null) throw new ServiceException(ServiceException.MISSING_DATA, "ENTITY ID CAN NOT BE NULL");
		Optional<UserEntity> userEntityOptional = userRepository.findById(id);
		
		if(!userEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE FOUND AN ENTITY WITH ID %s", id));
		}
		
		return userEntityOptional.get();
	}
	
	@Transactional
	public UserEntity save(UserFrmDto userFrmDto) throws ServiceException {
		
		Integer personId = userFrmDto.getId();
		Optional<PersonEntity> personEntityOptional = personRepository.findById(personId);
		
		if(!personEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, 
					String.format("COULD NOT BE FOUND A PERSON ENTITY WITH ID %s", personId));
		}
		
		UserEntity userEntity = new UserEntity(personEntityOptional.get(), userFrmDto.getUsername(), userFrmDto.getPassword());
		
		userRepository.save(userEntity);
		
		return userEntity;
	}

	@Transactional
	public void delete(UserEntity userEntity) {
		userRepository.delete(userEntity);
	}
}
