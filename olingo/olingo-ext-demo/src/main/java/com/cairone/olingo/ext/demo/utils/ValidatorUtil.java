package com.cairone.olingo.ext.demo.utils;

import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

import com.cairone.olingo.ext.demo.exceptions.ODataBadRequestException;

public class ValidatorUtil {

	public static void validate(Validator validator, MessageSource messageSource, Object entityToValidate) throws ODataBadRequestException {
		
		DataBinder binder = new DataBinder(entityToValidate);
		
		binder.setValidator(validator);
		binder.validate();
		
		BindingResult bindingResult = binder.getBindingResult();
		
		if(bindingResult.hasFieldErrors()) {
			
			for (Object object : bindingResult.getAllErrors()) {
			    if(object instanceof FieldError) {
			        FieldError fieldError = (FieldError) object;
			        String message = messageSource.getMessage(fieldError, null);
			        throw new ODataBadRequestException(
			        		String.format("INVALID DATA IN REQUEST. %s", message));
			    }
			}
		}
	}
}
