package com.cairone.odataexample.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.odataexample.dtos.SectorFrmDto;

@Component
public class SectorFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (SectorFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		ValidationUtils.rejectIfEmpty(errors, "id", "required", new Object[] {"ID DEL SECTOR"});
		ValidationUtils.rejectIfEmpty(errors, "nombre", "required", new Object[] {"NOMBRE DEL SECTOR"});
	}
}
