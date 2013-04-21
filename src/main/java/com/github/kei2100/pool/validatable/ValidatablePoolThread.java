package com.github.kei2100.pool.validatable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kei2100.pool.Pool;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolException;
import com.github.kei2100.pool.ValidationConfig;
import com.github.kei2100.pool.util.NameableDaemonThreadFactory;
import com.github.kei2100.pool.util.PoolLoggerMarkerFactory;


class ValidatablePoolThread<T> {
	private static final Logger logger = LoggerFactory.getLogger(ValidatablePoolThread.class);
	
	private final Pool<T> pool;
	private final ValidationConfig config;
	
	private AtomicBoolean isScheduled = new AtomicBoolean(false); 
	private ScheduledExecutorService taskExecutor;	
	
	ValidatablePoolThread(Pool<T> pool, ValidationConfig config) {
		this.pool = pool;
		this.config = config;
	}
	
	void scheduleBackgroundValidate() {		
		if (isScheduled.get()) {
			throw new IllegalStateException("already scheduled");
		}
		if (!isScheduled.compareAndSet(false, true)) {
			throw new IllegalStateException("already scheduled");
		}
		
		taskExecutor = 
				Executors.newScheduledThreadPool(
						config.getTestThreads(), 
						new NameableDaemonThreadFactory(ValidatablePoolThread.class.getSimpleName()));
		
		ValidateTaskBootstrap bootstrap = new ValidateTaskBootstrap();
		taskExecutor.scheduleWithFixedDelay(
				bootstrap, 
				config.getTestThreadInitialDelayMillis(),
				config.getTestThreadIntervalMillis(), 
				TimeUnit.MILLISECONDS);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				taskExecutor.shutdownNow();
			}
		}));
	}	
	
	private class ValidateTaskBootstrap implements Runnable {
		private static final boolean DO_NOT_CREATE_NEW = false;
		
		private final Object dummy = new Object();		
		private ConcurrentMap<Integer, Object> alreadyValidatedCheckMap;
		
		@Override
		public void run() {
			int maxIdleEntries = pool.getPoolConfig().getMaxIdleEntries();
			alreadyValidatedCheckMap = new ConcurrentHashMap<Integer, Object>(maxIdleEntries);
			
			try {
				while (true) {
					PoolEntry<T> idleEntry = pool.tryBorrowEntry(DO_NOT_CREATE_NEW);

					if (idleEntry == null) {
						break;
					}
					if (isAlreadyValidated(idleEntry)) {
						pool.returnEntry(idleEntry);
						break;
					}
					
					taskExecutor.submit(new ValidateTask(idleEntry));
				}
			} catch (PoolException e) {
				logger.warn(PoolLoggerMarkerFactory.getMarker(), 
						"Failed to borrow idle entry.", e);
			}			
		}

		private boolean isAlreadyValidated(PoolEntry<T> idleEntry) {
			int hashCode = idleEntry.hashCode();
			Object result = alreadyValidatedCheckMap.putIfAbsent(hashCode, dummy);
			return (result != null); 
		}
	}
	
	private class ValidateTask implements Runnable {
		private final PoolEntry<T> idleEntry;
		
		private ValidateTask(PoolEntry<T> idleEntry) {
			this.idleEntry = idleEntry;
		}
		
		@Override
		public void run() {
			ValidationHelper.invalidateIfAgeExpired(config, idleEntry);
			// has not yet exceeded maxAge. 
			if (idleEntry.getState().isValid()) {
				ValidationHelper.validate(config, idleEntry);
			}
			pool.returnEntry(idleEntry);
		}
	}
		
}
