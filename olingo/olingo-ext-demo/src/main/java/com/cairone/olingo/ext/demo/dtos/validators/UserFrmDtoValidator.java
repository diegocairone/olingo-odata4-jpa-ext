package com.cairone.olingo.ext.demo.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.olingo.ext.demo.dtos.UserFrmDto;

@Component
public class UserFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (UserFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		ValidationUtils.rejectIfEmpty(errors, "id", "required", new Object[] {"ID"});
		ValidationUtils.rejectIfEmpty(errors, "username", "required", new Object[] {"USER NAME"});
		ValidationUtils.rejectIfEmpty(errors, "password", "required", new Object[] {"PASSWORD"});
	}
}
