package com.cairone.odataexample.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.odataexample.dtos.PaisFrmDto;

@Component
public class PaisFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (PaisFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		PaisFrmDto frmDto = (PaisFrmDto) target;
		
		ValidationUtils.rejectIfEmpty(errors, "id", "required", new Object[] {"ID"});
		ValidationUtils.rejectIfEmpty(errors, "nombre", "required", new Object[] {"NOMBRE"});
		
		if(frmDto.getPrefijo() != null && frmDto.getPrefijo() <= 0) {
			errors.rejectValue("prefijo", "invalid", new Object[] {"PREFIJO TELEFONICO"}, null);
		}
	}
}
