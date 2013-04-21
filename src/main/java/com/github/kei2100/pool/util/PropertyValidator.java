package com.github.kei2100.pool.util;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum PropertyValidator {
	
	INSTANCE;
	
	private static final Logger logger = LoggerFactory.getLogger(PropertyValidator.class);
	private Validator validator;
	
	private PropertyValidator() {
		try {
			validator = Validation.buildDefaultValidatorFactory().getValidator();
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(PropertyValidator.class);
			logger.warn(PoolLoggerMarkerFactory.getMarker(), 
					"Failed to get javax.validation.Validator implementation. Can not validation.", e);
		}
	}
	
	public <T> void validate(T object) throws PropertyValidationException {
		if (validator == null) {
			logger.warn(PoolLoggerMarkerFactory.getMarker(), 
					"Can not validation. validator is not set.");
			return;
		}
		
		Set<ConstraintViolation<T>> violations = validator.validate(object);
		
		if (violations.isEmpty()) return;
		
		StringBuilder messages = new StringBuilder();
		for (ConstraintViolation<T> violation : violations) {			
			messages.append(violation.getPropertyPath().toString())
			        .append(": ")
			        .append(violation.getMessage())
			        .append(".");
		}
		throw new PropertyValidationException(messages.toString());
	}
}
