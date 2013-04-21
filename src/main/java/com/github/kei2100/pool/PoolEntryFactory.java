package com.github.kei2100.pool;

/**
 * An interface defining factory method for {@link PoolEntry}.
 * 
 * @param <T> the type of included object in {@link PoolEntry}
 * */
public interface PoolEntryFactory<T> {
	
	/**
	 * Create {@link PoolEntry}. 
	 * 
	 * @return {@link PoolEntry}
	 * @throws Exception if some kind of exception occurred
	 * */
	PoolEntry<T> createPoolEntry() throws Exception;
}
