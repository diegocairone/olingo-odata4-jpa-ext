package com.cairone.odataexample.dtos.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.cairone.odataexample.dtos.UsuarioFrmDto;

@Component
public class UsuarioFrmDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return (UsuarioFrmDto.class).isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		ValidationUtils.rejectIfEmpty(errors, "tipoDocumentoId", "required", new Object[] {"ID DEL TIPO DE DOCUMENTO"});
		ValidationUtils.rejectIfEmpty(errors, "numeroDocumento", "required", new Object[] {"NUMERO DE DOCUMENTO"});
		ValidationUtils.rejectIfEmpty(errors, "nombreUsuario", "required", new Object[] {"NOMBRE DE USUARIO"});
		ValidationUtils.rejectIfEmpty(errors, "cuentaVencida", "required", new Object[] {"CUENTA VENCIDA"});
		ValidationUtils.rejectIfEmpty(errors, "claveVencida", "required", new Object[] {"CLAVE VENCIDA"});
		ValidationUtils.rejectIfEmpty(errors, "cuentaBloqueada", "required", new Object[] {"CUENTA BLOQUEADA"});
		ValidationUtils.rejectIfEmpty(errors, "usuarioHabilitado", "required", new Object[] {"USUARIO HABILITADO"});
	}
}
