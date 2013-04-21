package com.github.kei2100.pool.util;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.kei2100.pool.PooledObjectFactory;


public class ThrowExceptionObjectFactory<T> implements PooledObjectFactory<T> {
	private final Class<T> classToBeCreateInstance;
	private final int toBeExceptionCount;
	private final AtomicInteger count = new AtomicInteger(0);
	
	public ThrowExceptionObjectFactory(Class<T> classToBeCreateInstance, int toBeExceptionCount) {
		this.classToBeCreateInstance = classToBeCreateInstance;
		this.toBeExceptionCount = toBeExceptionCount; 
	}
	
	@Override
	public T createInstance() throws Exception {
		int increment = count.incrementAndGet();
		if (increment < toBeExceptionCount) {
			return classToBeCreateInstance.newInstance();
		}
		
		throw new InstantiationException();
	}
}
