package com.github.kei2100.pool.validatable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.kei2100.pool.Pool;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolException;
import com.github.kei2100.pool.ValidationConfig;


class CheckAgeExpiredListener<T> implements ValidatablePoolListener<T> {
		
	private final Pool<T> pool;
	private final ValidationConfig config;
	
	CheckAgeExpiredListener(Pool<T> pool, ValidationConfig config) {
		this.pool = pool;
		this.config = config;
	}
	
	@Override
	public PoolEntry<T> afterBorrowEntry(PoolEntry<T> entry, boolean createNew, long elapsedMillis) 
			throws InterruptedException, TimeoutException, PoolException {
		
		if (!config.isTestOnBorrow()) return entry;
		
		final long methodStartedAt = System.currentTimeMillis();
		ValidationHelper.invalidateIfAgeExpired(config, entry);
		if (entry.getState().isValid()) return entry;
		
		do {
			pool.returnEntry(entry);			
			entry = borrowEntryAgain(createNew, elapsedMillis, methodStartedAt);
			if (entry == null) {
				break;
			}
			ValidationHelper.invalidateIfAgeExpired(config, entry);
		} while (!entry.getState().isValid());
		
		return entry;
	}

	private PoolEntry<T> borrowEntryAgain(boolean createNew, long elapsedMillis, long methodStartedAt)
			throws InterruptedException, TimeoutException, PoolException {
		
		if (pool.getPoolConfig().isWaitUnlimitOnBorrow()) {
			return pool.borrowEntry(createNew);
		} 
		
		long elapsedMillisThisBlock = System.currentTimeMillis() - methodStartedAt;
		long remainingMillis = pool.getPoolConfig().getMaxWaitMillisOnBorrow() - 
				(elapsedMillis + elapsedMillisThisBlock);

		if (remainingMillis < 1) {
			throw new TimeoutException("borrowEntry timed out.");
		}
		return pool.borrowEntry(createNew, remainingMillis, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public PoolEntry<T> afterTryBorrowEntry(PoolEntry<T> entry, boolean createNew) throws PoolException {
		if (!config.isTestOnBorrow()) return entry;
		
		ValidationHelper.invalidateIfAgeExpired(config, entry);
		if (entry.getState().isValid()) return entry;
		
		do {
			pool.returnEntry(entry);			
			entry = pool.tryBorrowEntry(createNew);
			if (entry == null) {
				break;
			}
			ValidationHelper.invalidateIfAgeExpired(config, entry);
		} while (!entry.getState().isValid());
		
		return entry;
	}
	
	@Override
	public PoolEntry<T> beforeReturnEntry(PoolEntry<T> entry) {
		if (!config.isTestOnReturn()) return entry;
		
		ValidationHelper.invalidateIfAgeExpired(config, entry);
		return entry;
	}
}
