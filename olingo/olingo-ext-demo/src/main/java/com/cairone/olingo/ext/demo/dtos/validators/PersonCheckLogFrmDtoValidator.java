package com.cairone.olingo.ext.demo.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.olingo.ext.demo.dtos.PersonCheckLogFrmDto;

@Component
public class PersonCheckLogFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (PersonCheckLogFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		ValidationUtils.rejectIfEmpty(errors, "personId", "required", new Object[] {"PERSON ID"});
		ValidationUtils.rejectIfEmpty(errors, "checkType", "required", new Object[] {"CHECK TYPE"});
		ValidationUtils.rejectIfEmpty(errors, "datetime", "required", new Object[] {"DATE AND TIME"});
	}
}
