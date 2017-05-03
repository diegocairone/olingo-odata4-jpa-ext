package com.cairone.odataexample.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.odataexample.dtos.TipoDocumentoFrmDto;

@Component
public class TipoDocumentoFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (TipoDocumentoFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		ValidationUtils.rejectIfEmpty(errors, "id", "required", new Object[] {"ID DEL TIPO DE DOCUMENTO"});
		ValidationUtils.rejectIfEmpty(errors, "nombre", "required", new Object[] {"NOMBRE DEL TIPO DE DOCUMENTO"});
		
	}

}
