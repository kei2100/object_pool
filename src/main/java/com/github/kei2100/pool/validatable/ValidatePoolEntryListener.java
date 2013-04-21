package com.github.kei2100.pool.validatable;

import java.util.concurrent.TimeoutException;

import com.github.kei2100.pool.Pool;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolException;
import com.github.kei2100.pool.ValidationConfig;


class ValidatePoolEntryListener<T> implements ValidatablePoolListener<T> {

	private final Pool<T> pool;
	private final ValidationConfig config;
	
	ValidatePoolEntryListener(Pool<T> pool, ValidationConfig config) {
		this.pool = pool;
		this.config = config;
	}
	
	@Override
	public PoolEntry<T> afterBorrowEntry(PoolEntry<T> entry, boolean createNew, long elapsedMillis) 
			throws InterruptedException, TimeoutException, PoolException {
		
		return validateAfterBorrow(entry);
	}

	@Override
	public PoolEntry<T> afterTryBorrowEntry(PoolEntry<T> entry, boolean createNew) 
			throws PoolException {
		
		return validateAfterBorrow(entry);
	}
	
	private PoolEntry<T> validateAfterBorrow(PoolEntry<T> entry) throws PoolException {
		if (!config.isTestOnBorrow()) return entry;
		if (entry == null) return entry;
		
		boolean validateSuccessful = ValidationHelper.validate(config, entry);
		if (validateSuccessful) {
			return entry;
		} else {
			pool.returnEntry(entry);
			throw new PoolException("PoolEntry is invalid.");
		}
	}

	@Override
	public PoolEntry<T> beforeReturnEntry(PoolEntry<T> entry) {
		if (config.isTestOnReturn()) {
			ValidationHelper.validate(config, entry);
		}
		return entry;
	}
}
