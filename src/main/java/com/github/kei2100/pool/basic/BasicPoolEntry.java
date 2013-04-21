package com.github.kei2100.pool.basic;

import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolEntryState;
import com.github.kei2100.pool.PooledObjectValidator;


public class BasicPoolEntry<T> implements PoolEntry<T> {
	
	private final T object;
	private final PoolEntryState state; 
	private final PooledObjectValidator<T> validator;
	
	protected BasicPoolEntry(T object, PooledObjectValidator<T> validator) {
		this.object = object;
		this.validator = validator;
		
		this.state = new PoolEntryState();
	}
		
	@Override
	public T getObject() {
		return object;
	}
	
	@Override
	public PoolEntryState getState() {
		return state;
	}
	
	// このエントリの有効性を確認する
	@Override
	public boolean validate() throws Exception {
		boolean expectValid = state.isValid();
		if (!expectValid) {
			// already invalidated
			return false;
		}
		
		boolean updateValid = validator.validate(object);
		boolean updateSuccessful = state.compareAndSetValid(expectValid, updateValid);
		
		// return true, if entry is not invalidated while setting the state
		if (updateValid && updateSuccessful) {
			state.setLastValidatedAt(System.currentTimeMillis());
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void invalidate() throws Exception {
		boolean expectValid = state.isValid();
		boolean updateSuccessful = state.compareAndSetValid(expectValid, false);
		
		// invalidate only once
		if (expectValid && updateSuccessful) {
			validator.invalidate(object);
		}
	}
}
