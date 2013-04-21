package com.github.kei2100.pool.util;

public class PropertyValidationException extends RuntimeException{
	
	private static final long serialVersionUID = 6467024091536458019L;

	public PropertyValidationException(String message) {
		super(message);
	}
	
	public PropertyValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
