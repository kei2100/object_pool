package com.github.kei2100.pool;

/**
 * A pool entry interface. 
 * @param <T> the type of included object in {@link PoolEntry}
 * */
public interface PoolEntry<T> {
	
	/**
	 * Get the object that is included in this {@link PoolEntry}.
	 * @return object 
	 * */
	T getObject();

	/**
	 * Get {@link PoolEntryState}.
	 * @return {@link PoolEntryState}
	 * */
	PoolEntryState getState();

	/**
	 * Check validity of the object that is included this {@link PoolEntry}.
	 * @return true, if can be confirmed validity of the object. otherwise, false.
	 * @throws Exception if some kind of exception occurred. 
	 * */
	boolean validate() throws Exception;
	
	/**
	 * Invalidate this {@link PoolEntry}, 
	 * with the object that is included this {@link PoolEntry}.
	 * @throws Exception if some kind of exception occurred   
	 * */
	void invalidate() throws Exception;
}