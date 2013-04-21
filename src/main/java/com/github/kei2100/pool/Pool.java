package com.github.kei2100.pool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An object pool interface. 
 * 
 * @param <T> the type of included object in {@link PoolEntry}
 * */
public interface Pool<T> {
	
	/**
	 * Return this pool's configuration.
	 * 
	 * @return {@link PoolConfig} 
	 * */
	PoolConfig getPoolConfig();
	
	/**
	 * Get {@link PoolEntry} from this pool. 
	 * <p>
	 * If remain idle entry in this pool, return the entry immediately.  
	 * Does not remain, perform the following operations.
	 * <li>If busy entries count have not reached {@link PoolConfig#getMaxActiveEntries()},  
	 * create new an instance, and return it as pool entry.
	 * <li>If busy entries count have reached {@link PoolConfig#getMaxActiveEntries()},
	 * wait for until the entry is returned to this pool by other threads.   
	 * Time to wait is {@link PoolConfig#getMaxWaitMillisOnBorrow()}.
	 * </p>
	 * @return {@link PoolEntry}
	 * @throws InterruptedException if the current thread is interrupted while waiting for return entry.
	 * @throws TimeoutException if the waiting time exceeded {@link PoolConfig#getMaxWaitMillisOnBorrow()}.
	 * @throws PoolException if an exception occurs for other reasons. 
	 * */
	PoolEntry<T> borrowEntry() 
			throws InterruptedException, TimeoutException, PoolException;
	
	/**
	 * Get {@link PoolEntry} from this pool.
	 * <p>
	 * If does not remain idle entry in this pool, 
	 * determine whether to create a new entry by the value of parameter.   
	 * </p>
	 * @param createNew if true then create a new entry.
	 * @return {@link PoolEntry}. or null, if does not remain idle entry in this pool and createNew is false. 
	 * @see #borrowEntry()
	 * */
	PoolEntry<T> borrowEntry(boolean createNew) 
			throws InterruptedException, TimeoutException, PoolException;
	
	/**
	 * Get {@link PoolEntry} from this pool.
	 * <p>
	 * If busy entries count have reached {@link PoolConfig#getMaxActiveEntries()},  
	 * wait for until the entry is returned to this pool by other threads.
	 * Time to wait is specified by the parameters.
	 * </p>
	 * @param timeout the maximum time to wait 
	 * @param unit the time unit of the timeout argument
	 * @return {@link PoolEntry}
	 * @see #borrowEntry()
	 * */
	PoolEntry<T> borrowEntry(long timeout, TimeUnit unit) 
			throws InterruptedException, TimeoutException, PoolException;
	
	/**
	 * Get {@link PoolEntry} from this pool.
	 *  
	 * @param createNew if true then create a new entry.
	 * @param timeout the maximum time to wait 
	 * @param unit the time unit of the timeout argument
	 * @return {@link PoolEntry} and null if does not remain idle entry in this pool and createNew is false. 
	 * @see #borrowEntry(boolean)
	 * @see #borrowEntry(long, TimeUnit)
	 * */
	PoolEntry<T> borrowEntry(boolean createNew, long timeout, TimeUnit unit) 
			throws InterruptedException, TimeoutException, PoolException;
	
	/**
	 * Get {@link PoolEntry} from this pool, only if can get the entry immediately.
	 * 
	 * @return {@link PoolEntry} if can get the entry immediately and null otherwise.
	 * @throws PoolException if exception of Something occurs
	 * */
	PoolEntry<T> tryBorrowEntry() throws PoolException;
	
	/**
	 * Get {@link PoolEntry} from this pool, only if can get the entry immediately.
	 * <p>
	 * If does not remain idle entry in this pool, 
	 * determine whether to create a new entry by the value of parameter.   
	 * </p>
	 * @param createNew if true then create a new entry.
	 * @return {@link PoolEntry} if can get the idle entry immediately and null otherwise.
	 * @throws PoolException if exception of Something occurs
	 * */
	PoolEntry<T> tryBorrowEntry(boolean createNew) throws PoolException;
	
	/**
	 * Return {@link PoolEntry} to this pool.
	 * <p>
	 * Always, must return {@link PoolEntry} obtained from this pool.
	 * If return {@link PoolEntry} that is instantiated by the external, 
	 * state of this pool will be illegal.
	 * </p>
	 * @param entry for return
	 * @throws NullPointerException if entry is null.
	 * */
	void returnEntry(PoolEntry<T> entry) throws NullPointerException;
}