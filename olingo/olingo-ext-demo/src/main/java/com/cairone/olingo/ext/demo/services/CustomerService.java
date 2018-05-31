package com.cairone.olingo.ext.demo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cairone.olingo.ext.demo.entities.CustomerEntity;
import com.cairone.olingo.ext.demo.exceptions.ServiceException;
import com.cairone.olingo.ext.demo.repositories.CustomerRepository;

@Service
public class CustomerService {

	@Autowired private CustomerRepository customerRepository = null;
	
	@Transactional(readOnly=true)
	public CustomerEntity findOne(Integer id) throws ServiceException {
		
		if(id == null) throw new ServiceException(ServiceException.MISSING_DATA, "ENTITY ID CAN NOT BE NULL");
		Optional<CustomerEntity> customerEntityOptional = customerRepository.findById(id);
		
		if(!customerEntityOptional.isPresent()) {
			throw new ServiceException(ServiceException.NOT_FOUND, String.format("COULD NOT BE FOUND AN ENTITY WITH ID %s", id));
		}
		
		return customerEntityOptional.get();
	}

	public CustomerRepository getCustomerRepository() {
		return customerRepository;
	}
	
}
