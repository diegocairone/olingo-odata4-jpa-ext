package com.cairone.olingo.ext.demo.exceptions;

import java.util.Locale;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

public class ODataBadRequestException extends ODataApplicationException {

	private static final long serialVersionUID = 1L;
	private String message = null;
	
	public ODataBadRequestException(String message) {
		super(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
		this.message = message;
	}
	
	public ODataBadRequestException(String oDataErrorCode, String message) {
		super(message, HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH, oDataErrorCode);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
