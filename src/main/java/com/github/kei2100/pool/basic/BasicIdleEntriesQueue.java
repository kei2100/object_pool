package com.github.kei2100.pool.basic;

import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kei2100.pool.IdleEntriesQueue;
import com.github.kei2100.pool.PoolConfig;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.util.PoolLoggerMarkerFactory;


public class BasicIdleEntriesQueue<T> implements IdleEntriesQueue<T>{
	private static final Logger logger = LoggerFactory.getLogger(BasicIdleEntriesQueue.class);
	private final ArrayBlockingQueue<PoolEntry<T>> idleEntries;

	public BasicIdleEntriesQueue(PoolConfig config) {
		idleEntries = new ArrayBlockingQueue<PoolEntry<T>>(config.getMaxIdleEntries());
	}
	
	@Override
	public PoolEntry<T> poll() {
		PoolEntry<T> idle = idleEntries.poll();
		return idle;
	}
	
	@Override
	public boolean offer(PoolEntry<T> entry) throws NullPointerException {
		if (entry == null) throw new NullPointerException("entry is null.");
		if (!entry.getState().isValid()) return false;		
		
		boolean offerSuccessful = idleEntries.offer(entry);
		if (!offerSuccessful) {
			invalidateEntry(entry);
		}
		
		return offerSuccessful;
	}

	private void invalidateEntry(PoolEntry<T> entry) {
		try {
			entry.invalidate();
		} catch (Exception e) {
			logger.warn(PoolLoggerMarkerFactory.getMarker(), 
					"Invalidate PoolEntry throws Exception.", e);
		}
	}
	
	/*
	 * This method is typically used for debugging and testing purposes.
	 * */	
	public int getIdleEntriesCount() {
		return idleEntries.size();
	}
}
