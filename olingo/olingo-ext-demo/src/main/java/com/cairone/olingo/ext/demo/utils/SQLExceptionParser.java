package com.cairone.olingo.ext.demo.utils;

import java.sql.SQLException;

import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.UnexpectedRollbackException;

public class SQLExceptionParser {

	public static final SQLException getSQLException(Exception e) {
		if(e instanceof DataIntegrityViolationException) {
			DataIntegrityViolationException dataIntegrityViolationException = (DataIntegrityViolationException) e;
			if(dataIntegrityViolationException.getCause() != null && dataIntegrityViolationException.getCause() instanceof ConstraintViolationException) {
				ConstraintViolationException constraintViolationException = (ConstraintViolationException) dataIntegrityViolationException.getCause();
				if(constraintViolationException.getCause() != null && constraintViolationException.getCause() instanceof SQLException) {
					SQLException sqlException = (SQLException) constraintViolationException.getCause();
					return sqlException;
				}
			}
		}
		if(e instanceof UnexpectedRollbackException) {
			UnexpectedRollbackException unexpectedRollbackException = (UnexpectedRollbackException) e;
			if(unexpectedRollbackException.getCause() != null && unexpectedRollbackException.getCause() instanceof RollbackException) {
				RollbackException rollbackException = (RollbackException) unexpectedRollbackException.getCause();
				if(rollbackException.getCause() != null && rollbackException.getCause() instanceof PersistenceException) {
					PersistenceException persistenceException = (PersistenceException) rollbackException.getCause();
					if(persistenceException.getCause() != null && persistenceException.getCause() instanceof ConstraintViolationException) {
						ConstraintViolationException constraintViolationException = (ConstraintViolationException) persistenceException.getCause();
						if(constraintViolationException.getCause() != null && constraintViolationException.getCause() instanceof SQLException) {
							SQLException sqlException = (SQLException) constraintViolationException.getCause();
							return sqlException;
						}	
					} else if(persistenceException.getCause() != null && persistenceException.getCause() instanceof DataException) {
						DataException dataException = (DataException) persistenceException.getCause();
						if(dataException.getCause() != null && dataException.getCause() instanceof SQLException) {
							SQLException sqlException = (SQLException) dataException.getCause();
							return sqlException;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static final String parse(Exception e) {
		SQLException sqlException = getSQLException(e);
		if(sqlException == null) {
			return e.getMessage();
		} else {
			return sqlException.getMessage();
		}
	}
}
