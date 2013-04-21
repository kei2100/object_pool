package com.github.kei2100.pool;

import java.util.concurrent.TimeUnit;

import com.github.kei2100.pool.Pool;
import com.github.kei2100.pool.PoolConfig;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolFactory;
import com.github.kei2100.pool.PooledObjectFactory;
import com.github.kei2100.pool.PooledObjectValidator;
import com.github.kei2100.pool.ValidationConfig;

public class TestRunningPool {
	public static void main(String[] args) throws Exception {
		PoolConfig poolConfig = new PoolConfig();
		poolConfig.setMaxActiveEntries(10);
		poolConfig.setInitialEntries(5);
		poolConfig.setMaxIdleEntries(5);
		poolConfig.setMinIdleEntries(5);
		poolConfig.setInvalidateThreads(3);
		poolConfig.setInvalidateIntervalMillis(100);
		poolConfig.setEnsureThreads(3);
		poolConfig.setEnsureIntervalMillis(100);
		

		ValidationConfig config = new ValidationConfig();
		config.setTestOnBorrow(true);
		config.setTestIntervalMillis(500);
		config.setTestThreadIntervalMillis(300);
		config.setTestThreadInitialDelayMillis(300);
		config.setTestThreads(3);
		config.setMaxAgeMillis(1000);
				
		PoolFactory<String> poolFactory = 
				new PoolFactory<String>(poolConfig, config, new TestObjectFactory(), new TestObjectValidator());
		
		Pool<String> pool = poolFactory.createInstance();

		while(true) {
			PoolEntry<String> entry1 = pool.borrowEntry();
			PoolEntry<String> entry2 = pool.borrowEntry();
			PoolEntry<String> entry3 = pool.borrowEntry();
			PoolEntry<String> entry4 = pool.borrowEntry();
			PoolEntry<String> entry5 = pool.borrowEntry();
			TimeUnit.MILLISECONDS.sleep(500);
			
			pool.returnEntry(entry1);
			pool.returnEntry(entry2);
			pool.returnEntry(entry3);
			pool.returnEntry(entry4);
			pool.returnEntry(entry5);
			TimeUnit.MILLISECONDS.sleep(500);
		}		
	}
	
	public static class TestObjectFactory implements PooledObjectFactory<String> {
		@Override
		public String createInstance() throws Exception {
			String threadName = Thread.currentThread().getName();
			System.out.println(String.format("created		[%s]", threadName));
			return "created";
		}
	}
	
	public static class TestObjectValidator implements PooledObjectValidator<String> {
		@Override
		public boolean validate(String pooledObject) {
			String threadName = Thread.currentThread().getName();
			System.out.println(String.format("validated	[%s]", threadName));
			return true;
		}

		@Override
		public void invalidate(String pooledObject) throws Exception{
			String threadName = Thread.currentThread().getName();
			System.out.println(String.format("invalidated	[%s]", threadName));
			pooledObject = null;
		}
	}

}
