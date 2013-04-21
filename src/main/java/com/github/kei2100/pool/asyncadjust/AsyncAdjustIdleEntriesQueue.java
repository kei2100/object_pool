package com.github.kei2100.pool.asyncadjust;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kei2100.pool.IdleEntriesQueue;
import com.github.kei2100.pool.PoolConfig;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolEntryFactory;
import com.github.kei2100.pool.util.PoolLoggerMarkerFactory;


public class AsyncAdjustIdleEntriesQueue<T> implements IdleEntriesQueue<T> {	
	private static final Logger logger = LoggerFactory.getLogger(AsyncAdjustIdleEntriesQueue.class);

	private final PoolConfig config;
	
	private final ConcurrentLinkedQueue<PoolEntry<T>> idleEntries;
	private final AtomicInteger idleEntriesCount;	
	private final ConcurrentLinkedQueue<PoolEntry<T>> idleEntriesToBeInvalidate;
	
	private AsyncInvalidateThread<T> invalidateThread;
	private AsyncEnsureThread<T> ensureThread;
	private AtomicBoolean isScheduledEnsureThread = new AtomicBoolean(false);
	
	public AsyncAdjustIdleEntriesQueue(PoolConfig config, PoolEntryFactory<T> entryFactory) {
		this.config = config;
		
		idleEntries = new ConcurrentLinkedQueue<PoolEntry<T>>();
		idleEntriesCount = new AtomicInteger(0);
		idleEntriesToBeInvalidate = new ConcurrentLinkedQueue<PoolEntry<T>>();
		
		if (config.isInvalidateInBackground()) {
			invalidateThread = new AsyncInvalidateThread<T>(config, this);
			invalidateThread.startBackgroundInvalidate();
		}
		if (config.isEnsureInBackground()) {
			ensureThread = new AsyncEnsureThread<T>(config, this, entryFactory);
		}
	}
	
	@Override
	public PoolEntry<T> poll() {
		PoolEntry<T> idle = idleEntries.poll();
		if (idle == null) {
			return idleEntriesToBeInvalidate.poll();
		}
		
		// Move toBeInvalidate to idle, if toBeInvalidate is not null.
		PoolEntry<T> toBeInvalidate = idleEntriesToBeInvalidate.poll();
		if (toBeInvalidate != null) {
			idleEntries.add(toBeInvalidate);
		} else {
			idleEntriesCount.decrementAndGet();
			scheduleBackgroundEnsureIfNeed(idle);
		}
		return idle;
	}

	private void scheduleBackgroundEnsureIfNeed(PoolEntry<T> idle) {
		if (!config.isEnsureInBackground()) return;
		if (countNeedForEnsure() < 1) return;
		
		boolean expect = isScheduledEnsureThread.get();
		if (expect) return;
		
		// schedule only once
		boolean setSuccessful = isScheduledEnsureThread.compareAndSet(expect, true);
		if (setSuccessful) {
			ensureThread.scheduluBackgroundEnsure();
		}
	}
	
	@Override
	public boolean offer(PoolEntry<T> entry) throws NullPointerException {
		if (entry == null) throw new NullPointerException("entry is null.");
		if (!entry.getState().isValid()) return false;
		
		int idleCount = idleEntriesCount.incrementAndGet();
		
		if (idleCount > config.getMaxIdleEntries()) {
			idleEntriesCount.decrementAndGet();
			innerInvalidate(entry);
			return false;
		} else {
			idleEntries.add(entry);
			return true;
		}
	}	
	
	private void innerInvalidate(PoolEntry<T> entry) {
		if (!config.isInvalidateInBackground()) {
			try {
				entry.invalidate();
			} catch (Exception e) {
				logger.warn(PoolLoggerMarkerFactory.getMarker(), 
						"Invalidate PoolEntry throws Exception.", e);
			}
		} else {
			idleEntriesToBeInvalidate.add(entry);
		}
	}
	
	PoolEntry<T> pollToBeInvalidate() {
		return idleEntriesToBeInvalidate.poll();
	}
	
	int countNeedForEnsure() {
		return config.getMinIdleEntries() - idleEntriesCount.get();
	}
}
