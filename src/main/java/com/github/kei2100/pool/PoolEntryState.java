package com.github.kei2100.pool;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manage the state of {@link PoolEntry}.
 * <p>
 * This class is thread safe.
 * </p>	
 * */
public class PoolEntryState {
	
	private final long createdAt = System.currentTimeMillis();
	
	private AtomicLong lastValidatedAt = new AtomicLong(System.currentTimeMillis());
	
	private AtomicBoolean valid = new AtomicBoolean(true);
	
	/**
	 * Get time {@link PoolEntry} was created.
	 * @return createdAt
	 * */
	public long getCreatedAt() {
		return createdAt;
	}
	
	/**
	 * Get the last time the validity {@link PoolEntry} has been confirmed.
	 * @return lastValidatedAt
	 * */
	public long getLastValidatedAt() {
		return lastValidatedAt.longValue();
	}
	/**
	 * Set lastValidatedAt.
	 * @param lastValidatedAt
	 * */
	public void setLastValidatedAt(long lastValidatedAt) {
		this.lastValidatedAt.set(lastValidatedAt);
	}
	
	/**
	 * Get validity of {@link PoolEntry}.
	 * */
	public boolean isValid() {
		return valid.get();
	}
	/**
	 * Compare and set valid
	 * @param expect
	 * @param update
	 * @return true, if succeeded
	 * @see AtomicBoolean#compareAndSet(boolean, boolean)
	 * */
	public boolean compareAndSetValid(boolean expect, boolean update) {
		return valid.compareAndSet(expect, update);
	}
}
