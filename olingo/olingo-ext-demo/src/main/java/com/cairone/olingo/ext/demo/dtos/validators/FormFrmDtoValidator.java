package com.cairone.olingo.ext.demo.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.olingo.ext.demo.dtos.FormFrmDto;

@Component
public class FormFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (FormFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		ValidationUtils.rejectIfEmpty(errors, "id", "required", new Object[] {"ID"});
		ValidationUtils.rejectIfEmpty(errors, "name", "required", new Object[] {"NAME"});
	}
}
