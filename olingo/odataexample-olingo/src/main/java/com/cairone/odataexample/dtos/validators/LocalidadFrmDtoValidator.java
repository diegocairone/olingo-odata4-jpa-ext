package com.cairone.odataexample.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.odataexample.dtos.LocalidadFrmDto;

@Component
public class LocalidadFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (LocalidadFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		LocalidadFrmDto frmDto = (LocalidadFrmDto) target;
		
		ValidationUtils.rejectIfEmpty(errors, "paisId", "required", new Object[] {"ID DE PAIS"});
		ValidationUtils.rejectIfEmpty(errors, "provinciaId", "required", new Object[] {"ID DE PROVINCIA"});
		ValidationUtils.rejectIfEmpty(errors, "localidadId", "required", new Object[] {"ID DE LOCALIDAD"});
		ValidationUtils.rejectIfEmpty(errors, "nombre", "required", new Object[] {"NOMBRE DE LA LOCALIDAD"});

		if(frmDto.getPrefijo() != null && frmDto.getPrefijo() <= 0) {
			errors.rejectValue("prefijo", "invalid", new Object[] {"PREFIJO TELEFONICO"}, null);
		}

		if(frmDto.getCp() != null && frmDto.getCp() <= 0) {
			errors.rejectValue("cp", "invalid", new Object[] {"CODIGO POSTAL"}, null);
		}
	}
}
