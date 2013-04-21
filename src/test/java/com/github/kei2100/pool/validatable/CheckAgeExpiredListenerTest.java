package com.github.kei2100.pool.validatable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.kei2100.pool.PoolConfig;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.ValidationConfig;
import com.github.kei2100.pool.basic.BasicPool;
import com.github.kei2100.pool.basic.PoolTestUtil;
import com.github.kei2100.pool.util.SpyObject;
import com.github.kei2100.pool.validatable.CheckAgeExpiredListener;


public class CheckAgeExpiredListenerTest {
	
	@Test
	public void afterBorrowEntry_entryが期限切れでない場合() throws Exception {
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, new PoolConfig());
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		
		ValidationConfig validationConfig = new ValidationConfig();
		validationConfig.setMaxAgeMillis(30000);
		CheckAgeExpiredListener<SpyObject> listener = 
				new CheckAgeExpiredListener<SpyObject>(pool, validationConfig); 
		
		int beforeTestPermits = pool.availablePermits();
		
		boolean createNew = true;
		long elapsedMillis = 0;
		PoolEntry<SpyObject> testedEntry = listener.afterBorrowEntry(entry, createNew, elapsedMillis);
		
		int afterTestPermits = pool.availablePermits();
		
		assertEquals(beforeTestPermits, afterTestPermits);
		assertEquals(entry.getObject(), testedEntry.getObject());
	}
	
	@Test
	public void afterBorrowEntry_entryとpool内のentryが全て期限切れ_entryをcreateNewする場合() throws Exception {
		PoolConfig poolConfig = new PoolConfig();
		poolConfig.setMaxActiveEntries(3);
		poolConfig.setMaxIdleEntries(3);
		poolConfig.setInitialEntries(2);
		
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, poolConfig);
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		
		ValidationConfig validationConfig = new ValidationConfig();
		validationConfig.setMaxAgeMillis(1);
		CheckAgeExpiredListener<SpyObject> listener = 
				new CheckAgeExpiredListener<SpyObject>(pool, validationConfig); 
		
		TimeUnit.MILLISECONDS.sleep(1);
		long beforeTestAt = System.currentTimeMillis();
		TimeUnit.MILLISECONDS.sleep(1);
		
		boolean createNew = true;
		long elapsedMillis = 0;
		PoolEntry<SpyObject> expectCreatedNewEntry = listener.afterBorrowEntry(entry, createNew, elapsedMillis);
		
		int availablePermits = pool.availablePermits();
		
		assertTrue(beforeTestAt < expectCreatedNewEntry.getState().getCreatedAt());
		assertEquals(2, availablePermits);
	}

	@Test
	public void afterBorrowEntry_entryとpool内のentryが全て期限切れ_entryをcreateNewしない場合() throws Exception {
		PoolConfig poolConfig = new PoolConfig();
		poolConfig.setMaxActiveEntries(3);
		poolConfig.setMaxIdleEntries(3);
		poolConfig.setInitialEntries(2);
		
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, poolConfig);
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		
		ValidationConfig validationConfig = new ValidationConfig();
		validationConfig.setMaxAgeMillis(1);
		CheckAgeExpiredListener<SpyObject> listener = 
				new CheckAgeExpiredListener<SpyObject>(pool, validationConfig); 
		
		TimeUnit.MILLISECONDS.sleep(2);
		
		boolean notCreateNew = false;
		long elapsedMillis = 0;
		PoolEntry<SpyObject> expectCreatedNewEntry = listener.afterBorrowEntry(entry, notCreateNew, elapsedMillis);
		
		int availablePermits = pool.availablePermits();
		
		assertNull(expectCreatedNewEntry);
		assertEquals(3, availablePermits);
	}

	@Test
	public void afterTryBorrowEntry_entryが期限切れでない場合() throws Exception {
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, new PoolConfig());
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		
		ValidationConfig validationConfig = new ValidationConfig();
		validationConfig.setMaxAgeMillis(30000);
		CheckAgeExpiredListener<SpyObject> listener = 
				new CheckAgeExpiredListener<SpyObject>(pool, validationConfig); 
		
		int beforeTestPermits = pool.availablePermits();
		
		boolean createNew = true;
		PoolEntry<SpyObject> testedEntry = listener.afterTryBorrowEntry(entry, createNew);
		
		int afterTestPermits = pool.availablePermits();
		
		assertEquals(beforeTestPermits, afterTestPermits);
		assertEquals(entry.getObject(), testedEntry.getObject());
	}
	
	@Test
	public void afterTryBorrowEntry_entryとpool内のentryが全て期限切れ_entryをcreateNewする場合() throws Exception {
		PoolConfig poolConfig = new PoolConfig();
		poolConfig.setMaxActiveEntries(3);
		poolConfig.setMaxIdleEntries(3);
		poolConfig.setInitialEntries(2);
		
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, poolConfig);
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		
		ValidationConfig validationConfig = new ValidationConfig();
		validationConfig.setMaxAgeMillis(1);
		CheckAgeExpiredListener<SpyObject> listener = 
				new CheckAgeExpiredListener<SpyObject>(pool, validationConfig); 
		
		TimeUnit.MILLISECONDS.sleep(1);
		long beforeTestAt = System.currentTimeMillis();
		TimeUnit.MILLISECONDS.sleep(1);
		
		boolean createNew = true;
		PoolEntry<SpyObject> expectCreatedNewEntry = listener.afterTryBorrowEntry(entry, createNew);
		
		int availablePermits = pool.availablePermits();
		
		assertTrue(beforeTestAt < expectCreatedNewEntry.getState().getCreatedAt());
		assertEquals(2, availablePermits);
	}
	
	@Test
	public void afterTryBorrowEntry_entryとpool内のentryが全て期限切れ_entryをcreateNewしない場合() throws Exception {
		PoolConfig poolConfig = new PoolConfig();
		poolConfig.setMaxActiveEntries(3);
		poolConfig.setMaxIdleEntries(3);
		poolConfig.setInitialEntries(2);
		
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, poolConfig);
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		
		ValidationConfig validationConfig = new ValidationConfig();
		validationConfig.setMaxAgeMillis(1);
		CheckAgeExpiredListener<SpyObject> listener = 
				new CheckAgeExpiredListener<SpyObject>(pool, validationConfig); 
		
		TimeUnit.MILLISECONDS.sleep(2);
		
		boolean notCreateNew = false;
		PoolEntry<SpyObject> expectCreatedNewEntry = listener.afterTryBorrowEntry(entry, notCreateNew);
		
		int availablePermits = pool.availablePermits();
		
		assertNull(expectCreatedNewEntry);
		assertEquals(3, availablePermits);
	}	
}
