package com.github.kei2100.pool.basic;

import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolEntryFactory;
import com.github.kei2100.pool.PooledObjectFactory;
import com.github.kei2100.pool.PooledObjectValidator;


public class BasicPoolEntryFactory<T> implements PoolEntryFactory<T> {

	private final PooledObjectFactory<T> objectFactory;
	private final PooledObjectValidator<T> validator;
	
	public BasicPoolEntryFactory(PooledObjectFactory<T> objectFactory, PooledObjectValidator<T> validator) {
		this.objectFactory = objectFactory;
		this.validator = validator;
	}
	
	@Override
	public PoolEntry<T> createPoolEntry() throws Exception {
		T object = null;
		try {
			object = objectFactory.createInstance();
			return new BasicPoolEntry<T>(object, validator);
		} catch (Exception e) {
			throw e;
		}
	}
}
