package com.cairone.olingo.ext.demo.utils;

import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.UnexpectedRollbackException;

import com.cairone.olingo.ext.demo.exceptions.ODataBadRequestException;
import com.cairone.olingo.ext.demo.exceptions.ODataInternalServerErrorException;
import com.cairone.olingo.ext.demo.exceptions.ODataResourceNotFoundException;
import com.cairone.olingo.ext.demo.exceptions.ServiceException;

public class OdataExceptionParser {
	
	public static final ODataApplicationException parse(Exception e) {
		if(e instanceof DataIntegrityViolationException || e instanceof UnexpectedRollbackException) {
			String message = SQLExceptionParser.parse(e);
			return new ODataInternalServerErrorException(message);
		} else if(e instanceof ServiceException) {
			ServiceException serviceException = (ServiceException) e;
			switch(serviceException.getCode()) {
			case ServiceException.NOT_FOUND:
				return new ODataResourceNotFoundException(e.getMessage());
			default:
				return new ODataBadRequestException(e.getMessage());
			}
		} else if(e instanceof ODataApplicationException) {
			return (ODataApplicationException) e;
		}
		return new ODataInternalServerErrorException(e.getMessage());
	}
}
