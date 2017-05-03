package com.cairone.odataexample.utils;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public class SQLExceptionParser {

	public static final String parse(Exception e) {
		if(e instanceof DataIntegrityViolationException) {
			DataIntegrityViolationException dataIntegrityViolationException = (DataIntegrityViolationException) e;
			if(dataIntegrityViolationException.getCause() != null && dataIntegrityViolationException.getCause() instanceof ConstraintViolationException) {
				ConstraintViolationException constraintViolationException = (ConstraintViolationException) dataIntegrityViolationException.getCause();
				if(constraintViolationException.getCause() != null && constraintViolationException.getCause() instanceof SQLException) {
					SQLException sqlException = (SQLException) constraintViolationException.getCause();
					return sqlException.getMessage();
				}
			}
		}
		return e.getMessage();
	}
}
