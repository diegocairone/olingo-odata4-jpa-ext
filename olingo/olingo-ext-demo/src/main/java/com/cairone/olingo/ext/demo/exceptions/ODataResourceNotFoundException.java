package com.cairone.olingo.ext.demo.exceptions;

import java.util.Locale;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

public class ODataResourceNotFoundException extends ODataApplicationException {
	
	private static final long serialVersionUID = 1L;
	private String message = null;
	
	public ODataResourceNotFoundException(String message) {
		super(message, HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		this.message = message;
	}
	
	public ODataResourceNotFoundException(String oDataErrorCode, String message) {
		super(message, HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH, oDataErrorCode);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
