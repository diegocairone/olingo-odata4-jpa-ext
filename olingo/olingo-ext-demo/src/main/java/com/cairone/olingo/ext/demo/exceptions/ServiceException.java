package com.cairone.olingo.ext.demo.exceptions;


public class ServiceException extends Exception {

	private static final long serialVersionUID = 1L;
	public static final int UNKNOWN = 0;
	public static final int NOT_FOUND = 1;
	public static final int DATA_INTEGRITY_VIOLATION = 2;
	public static final int MISSING_DATA = 3;
	
	private int code;
	
	public ServiceException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public ServiceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
	
	public ServiceException(String message) {
		this(UNKNOWN, message);
	}

	public ServiceException(String message, Throwable cause) {
		this(UNKNOWN, message, cause);
    }
	
	public int getCode() {
		return code;
	}
}
