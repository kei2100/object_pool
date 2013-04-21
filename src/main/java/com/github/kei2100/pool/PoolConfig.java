package com.github.kei2100.pool;

import javax.validation.constraints.Min;

import com.github.kei2100.pool.util.PropertyValidationException;
import com.github.kei2100.pool.util.PropertyValidator;


/**
 * Configuration of {@link Pool}. 
 * */
public class PoolConfig {
	private static byte WAIT_UNLIMIT_ON_BORROW = 0;
	
	/**
	 * The maximum number of {@link PoolEntry} 
	 * that can be allocated by the {@link Pool} 
	 * (checked out to clients, or idle awaiting checkout) at a given time.   
	 * */
	@Min(1)
	private int maxActiveEntries = 8;
	
	/**
	 * The number of {@link PoolEntry} to be created 
	 * when {@link Pool} is instantiated.
	 * */
	@Min(0)
	private int initialEntries = 0;
	
	/**
	 * The maximum number of {@link PoolEntry} 
	 * that can sit idle in the {@link Pool} at any time. 
	 * */
	@Min(1)
	private int maxIdleEntries = 8;
	
	/**
	 * The amount of millis to wait for {@link PoolEntry} is returned 
	 * in {@link Pool#borrowEntry()} method. 
	 * */
	@Min(0)
	private long maxWaitMillisOnBorrow = 1000;	// 0 is forever wait to borrow.
	
	/**
	 * The number of threads to invalidate {@link PoolEntry}.
	 * If the idle {@link PoolEntry} in the pool is greater than {@link #maxIdleEntries},
	 * these threads will do invalidation in the background.
	 * If this value is zero, invalidation of {@link PoolEntry} is performed by the client threads
	 * at the time you return the {@link PoolEntry} to the {@link Pool}.
	 * */
	@Min(0)
	private int invalidateThreads = 0;

	/** 
	 * The time to delay first execution of invalidate threads.
	 * */
	@Min(0)
	private long invalidateThreadInitialDelayMillis = 1000;
	
	/**
	 * The invalidate threads execution interval
	 * */
	@Min(1)
	private long invalidateIntervalMillis = 1000;

	/**
	 * The minimum number of {@link PoolEntry} ensured in the {@link Pool}.
	 * Ensure is performed by {@link #ensureThreads}. 
	 * This setting has no effect if the {@link #ensureThreads} is zero.
	 * */
	@Min(0)
	private int minIdleEntries = 0;
	
	/**
	 * The number of threads that ensure {@link PoolEntry} in the {@link Pool}.
	 * */
	@Min(0)
	private int ensureThreads = 0;
	
	/**
	 * The ensure threads execution interval
	 * */
	@Min(1)
	private long ensureIntervalMillis = 1000;
	
	/**
	 * Validate this configuration value.
	 * @throws PropertyValidationException If this configuration value is invalid. 
	 * */
	public void validateConfig() throws PropertyValidationException {
		validatePropValues();
		validatePropCorrelation();
	}
	
	private void validatePropValues() throws PropertyValidationException {
		PropertyValidator.INSTANCE.validate(this);
	}
	
	private void validatePropCorrelation() throws PropertyValidationException {
		if (maxActiveEntries < initialEntries) 
			throw new PropertyValidationException ("maxActiveEntries < initialEntries");
		if (maxActiveEntries < maxIdleEntries)
			throw new PropertyValidationException("maxActiveEntries < maxIdleEntries");
		
		if (maxActiveEntries < minIdleEntries)
			throw new PropertyValidationException("maxActiveEntries < minIdleEntries");
		if (maxIdleEntries < minIdleEntries) 
			throw new PropertyValidationException("maxIdleEntries < minIdleEntries");
	}
		
	public boolean isWaitUnlimitOnBorrow() {
		return maxWaitMillisOnBorrow == WAIT_UNLIMIT_ON_BORROW;
	}
	
	public boolean isInvalidateInBackground() {
		return (invalidateThreads > 0);
	}
	
	public boolean isEnsureInBackground() {
		return (ensureThreads > 0);
	}
	
	public int getMaxActiveEntries() {
		return maxActiveEntries;
	}
	public void setMaxActiveEntries(int maxActiveEntries) {
		this.maxActiveEntries = maxActiveEntries;
	}
	
	public int getInitialEntries() {
		return initialEntries;
	}
	public void setInitialEntries(int initialEntries) {
		this.initialEntries = initialEntries;
	}
	
	public long getMaxWaitMillisOnBorrow() {
		return maxWaitMillisOnBorrow;
	}
	public void setMaxWaitMillisOnBorrow(long maxWaitMillisOnBorrow) {
		this.maxWaitMillisOnBorrow = maxWaitMillisOnBorrow;
	}
	
	public int getMaxIdleEntries() {
		return maxIdleEntries;
	}
	public void setMaxIdleEntries(int maxIdleEntries) {
		this.maxIdleEntries = maxIdleEntries;
	}

	public int getMinIdleEntries() {
		return minIdleEntries;
	}
	public void setMinIdleEntries(int minIdleEntries) {
		this.minIdleEntries = minIdleEntries;
	}

	public int getInvalidateThreads() {
		return invalidateThreads;
	}
	public void setInvalidateThreads(int invalidateThreads) {
		this.invalidateThreads = invalidateThreads;
	}

	public long getInvalidateThreadInitialDelayMillis() {
		return invalidateThreadInitialDelayMillis;
	}
	public void setInvalidateThreadInitialDelayMillis(long invalidateThreadInitialDelayMillis) {
		this.invalidateThreadInitialDelayMillis = invalidateThreadInitialDelayMillis;
	}
	
	public long getInvalidateIntervalMillis() {
		return invalidateIntervalMillis;
	}
	public void setInvalidateIntervalMillis(long invalidateIntervalMillis) {
		this.invalidateIntervalMillis = invalidateIntervalMillis;
	}
		
	public int getEnsureThreads() {
		return ensureThreads;
	}
	public void setEnsureThreads(int ensureThreads) {
		this.ensureThreads = ensureThreads;
	}
	
	public long getEnsureIntervalMillis() {
		return ensureIntervalMillis;
	}
	public void setEnsureIntervalMillis(long ensureIntervalMillis) {
		this.ensureIntervalMillis = ensureIntervalMillis;
	}
}