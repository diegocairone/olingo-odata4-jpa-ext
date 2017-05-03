package com.cairone.odataexample.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.odataexample.dtos.PersonaFrmDto;

@Component
public class PersonaFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (PersonaFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmpty(errors, "tipoDocumentoId", "required", new Object[] {"ID DEL TIPO DE DOCUMENTO"});
		ValidationUtils.rejectIfEmpty(errors, "numeroDocumento", "required", new Object[] {"NUMERO DE DOCUMENTO"});
		ValidationUtils.rejectIfEmpty(errors, "nombres", "required", new Object[] {"NOMBRES"});
		ValidationUtils.rejectIfEmpty(errors, "apellidos", "required", new Object[] {"APELLIDOS"});
		ValidationUtils.rejectIfEmpty(errors, "paisId", "required", new Object[] {"ID DEL PAIS"});
		ValidationUtils.rejectIfEmpty(errors, "provinciaId", "required", new Object[] {"ID DE LA PROVINCIA"});
		ValidationUtils.rejectIfEmpty(errors, "localidadId", "required", new Object[] {"ID DE LA LOCALIDAD"});
		ValidationUtils.rejectIfEmpty(errors, "genero", "required", new Object[] {"GENERO"});
		
	}

}
