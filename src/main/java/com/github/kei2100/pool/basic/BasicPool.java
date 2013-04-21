package com.github.kei2100.pool.basic;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kei2100.pool.IdleEntriesQueue;
import com.github.kei2100.pool.Pool;
import com.github.kei2100.pool.PoolConfig;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolEntryFactory;
import com.github.kei2100.pool.PoolException;
import com.github.kei2100.pool.util.PoolLoggerMarkerFactory;



public class BasicPool<T> implements Pool<T> {
	private static final Logger logger = LoggerFactory.getLogger(BasicPool.class);
	
	private final PoolConfig config ;
	private final PoolEntryFactory<T> entryFactory;
	private final IdleEntriesQueue<T> idleEntries;
	
	private final Semaphore borrowingSemaphore;
		
	public BasicPool(PoolConfig config, IdleEntriesQueue<T> idleEntries, PoolEntryFactory<T> entryFactory) {
		this.config = config;
		this.idleEntries = idleEntries;
		this.entryFactory = entryFactory;
		
		borrowingSemaphore = new Semaphore(config.getMaxActiveEntries());
		
		// initialize idle entries
		for (int i = 0; i < config.getInitialEntries(); i++) {
			try {
				idleEntries.offer(createIdleEntry());
			} catch (Exception e) {
				logger.warn(PoolLoggerMarkerFactory.getMarker(), 
						"Failed to create initial pool entry.", e);
			}
		}
	}

	@Override
	public PoolConfig getPoolConfig() {
		return config;
	}
	
	@Override
	public PoolEntry<T> borrowEntry() 
			throws InterruptedException, TimeoutException, PoolException {

		return borrowEntry(true);
	}
	
	@Override
	public PoolEntry<T> borrowEntry(boolean createNew)
			throws InterruptedException, TimeoutException, PoolException {

		long timeout = config.getMaxWaitMillisOnBorrow();
		return borrowEntry(createNew, timeout, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public PoolEntry<T> borrowEntry(long timeout, TimeUnit unit)
			throws InterruptedException, TimeoutException, PoolException {

		return borrowEntry(true, timeout, unit);
	}
	
	@Override
	public PoolEntry<T> borrowEntry(boolean createNew, long timeout, TimeUnit unit) 
			throws InterruptedException, TimeoutException, PoolException {

		try {
			if (config.isWaitUnlimitOnBorrow()) {
				borrowingSemaphore.acquire();
			} else {
				boolean acquireSuccess = borrowingSemaphore.tryAcquire(timeout, unit);
				if (!acquireSuccess) {
					// pool entries all busy
					throw new TimeoutException("borrowEntry timed out.");
				}
			}
		} catch (InterruptedException e) {
			throw e;
		}
		
		return innerBorrowEntry(createNew);
	}

	
	@Override
	public PoolEntry<T> tryBorrowEntry() throws PoolException {
		return tryBorrowEntry(true);
	}
	
	@Override
	public PoolEntry<T> tryBorrowEntry(boolean createNew) throws PoolException {
		boolean acquireSuccess = borrowingSemaphore.tryAcquire();
		if (!acquireSuccess) {
			return null;
		}
		
		return innerBorrowEntry(createNew);
	}
	
	private PoolEntry<T> innerBorrowEntry(boolean createNew) throws PoolException {
		try {
			PoolEntry<T> entry = idleEntries.poll();
			if (entry == null && createNew) {
				entry = createIdleEntry();
			}
			
			if (entry == null) {
				borrowingSemaphore.release();
			}
			return entry;
		} catch (Exception e) {
			borrowingSemaphore.release();
			throw new PoolException(e);
		}
	}

	private PoolEntry<T> createIdleEntry() throws Exception {
		return entryFactory.createPoolEntry();
	}
	
	@Override
	public void returnEntry(PoolEntry<T> entry) throws NullPointerException {
		if (entry == null) throw new NullPointerException("entry is null");
		
		try{
			idleEntries.offer(entry);
		} finally { 
			borrowingSemaphore.release();
		}
	}
					
	/*
	 * This method is typically used for debugging and testing purposes.
	 * */
	public int availablePermits() {
		return borrowingSemaphore.availablePermits();
	}	
}
