package com.cairone.odataexample.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.odataexample.dtos.ProvinciaFrmDto;

@Component
public class ProvinciaFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (ProvinciaFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		ValidationUtils.rejectIfEmpty(errors, "paisID", "required", new Object[] {"ID DE PAIS"});
		ValidationUtils.rejectIfEmpty(errors, "id", "required", new Object[] {"ID DE PROVINCIA"});
		ValidationUtils.rejectIfEmpty(errors, "nombre", "required", new Object[] {"NOMBRE DE LA PROVINCIA"});
	}

}
