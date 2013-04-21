package com.github.kei2100.pool.validatable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.ValidationConfig;
import com.github.kei2100.pool.util.PoolLoggerMarkerFactory;


class ValidationHelper {
	private static final Logger logger = LoggerFactory.getLogger(ValidationHelper.class);
	
	static <T> boolean validate(ValidationConfig config, PoolEntry<T> entry) {
		long lastValidatedAt = entry.getState().getLastValidatedAt();
		
		if (config.isTestWithInterval()) {
			if (!isIntervalElapses(config, lastValidatedAt)) {
				return true;
			}
		}
		
		boolean validateSuccessful = innerValidate(entry);
		if (validateSuccessful) {
			return true;
		} else {
			innerInvalidate(entry);
			return false;
		}
	}
	
	static <T> void invalidateIfAgeExpired(ValidationConfig config, PoolEntry<T> entry) {
		if (config.isMaxAgeUnlimit()) {
			return;
		}
		
		long createdAt = entry.getState().getCreatedAt();
		long maxAgeMillis = config.getMaxAgeMillis();
				
		boolean hasAgeExpired = (System.currentTimeMillis() - createdAt) > maxAgeMillis;
		if (hasAgeExpired) {
			innerInvalidate(entry);
		}
	}

	private static <T> boolean innerValidate(PoolEntry<T> entry) {
		try {
			boolean validateSuccessful = entry.validate();
			return validateSuccessful;
		} catch (Exception e) {
			logger.warn(PoolLoggerMarkerFactory.getMarker(), 
					"Failed to validate pool entry. Pool entry will be invalidate. ", e);
			
			innerInvalidate(entry);
			return false;
		}
	}
	
	private static <T> void innerInvalidate(PoolEntry<T> entry) {
		try {
			entry.invalidate();
		} catch (Exception e) {
			logger.warn(PoolLoggerMarkerFactory.getMarker(), 
					"Failed to invalidate pool entry.", e);
		}
	}
	
	private static boolean isIntervalElapses(ValidationConfig config, long lastValidatedAt) {				
		long testIntervalMillis = config.getTestIntervalMillis();
		long now = System.currentTimeMillis();
		
		return testIntervalMillis < (now - lastValidatedAt);
	}
}
