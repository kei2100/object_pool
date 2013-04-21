package com.github.kei2100.pool;

/**
 * An interface defining factory method 
 * for object to be served by {@link Pool}.
 * <p>
 * The instance of implementing this interface is accessed by multiple threads. 
 * </p>
 * */
public interface PooledObjectFactory<T> {

	/**
	 * Create an instance. 
	 * 
	 * @return instance
	 * @throws Exception if some kind of exception occurred
	 * */
	T createInstance() throws Exception;
}
