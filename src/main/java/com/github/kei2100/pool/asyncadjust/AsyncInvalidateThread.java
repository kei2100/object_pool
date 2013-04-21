package com.github.kei2100.pool.asyncadjust;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.kei2100.pool.PoolConfig;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.util.NameableDaemonThreadFactory;


class AsyncInvalidateThread<T> {
	
	private final PoolConfig config;
	private final AsyncAdjustIdleEntriesQueue<T> queue; 
	
	private AtomicBoolean isScheduled = new AtomicBoolean(false);
	private ScheduledExecutorService taskExecutor;
	
	AsyncInvalidateThread(PoolConfig config, AsyncAdjustIdleEntriesQueue<T> queue) {
		this.config = config;
		this.queue = queue;
	}
	
	void startBackgroundInvalidate() {
		if (isScheduled.get()) {
			throw new IllegalStateException("already scheduled");
		}
		if (!isScheduled.compareAndSet(false, true)) {
			throw new IllegalStateException("already scheduled");
		}
		
		taskExecutor = 
				Executors.newScheduledThreadPool(
						config.getInvalidateThreads(), 
						new NameableDaemonThreadFactory(AsyncInvalidateThread.class.getSimpleName()));
				
		InvalidateTaskBootstrap bootstrap = new InvalidateTaskBootstrap();
		taskExecutor.scheduleWithFixedDelay(
				bootstrap, 
				config.getInvalidateThreadInitialDelayMillis(),
				config.getInvalidateIntervalMillis(), 
				TimeUnit.MILLISECONDS);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				taskExecutor.shutdownNow();
			}
		}));
	}
	
	private class InvalidateTaskBootstrap implements Runnable {
		@Override
		public void run() {
			while (true) {
				PoolEntry<T> entry = queue.pollToBeInvalidate();
				if (entry == null) break;
				
				taskExecutor.submit(new InvalidateTask(entry));
			}
		}
	}
	
	private class InvalidateTask implements Runnable {
		private final PoolEntry<T> entry;
		
		private InvalidateTask(PoolEntry<T> entry) {
			this.entry = entry;
		}
		
		@Override
		public void run() {
			try {
				entry.invalidate();
			} catch (Exception e) {
				// Logger
				e.printStackTrace();
			}
		}
	}
}

