package com.github.kei2100.pool.validatable;

import org.junit.Test;

import com.github.kei2100.pool.IdleEntriesQueue;
import com.github.kei2100.pool.PoolConfig;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolEntryFactory;
import com.github.kei2100.pool.PoolException;
import com.github.kei2100.pool.ValidationConfig;
import com.github.kei2100.pool.basic.BasicPool;
import com.github.kei2100.pool.basic.PoolTestUtil;
import com.github.kei2100.pool.util.SpyObject;
import com.github.kei2100.pool.util.SpyObjectFactory;
import com.github.kei2100.pool.util.SpyObjectValidator;
import com.github.kei2100.pool.validatable.ValidatablePoolListener;
import com.github.kei2100.pool.validatable.ValidatePoolEntryListener;

import static org.junit.Assert.*;


public class ValidatePoolEntryListenerTest {
	
	@Test
	public void afterBorrowEntry_validation成功の場合() throws Exception {
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, new PoolConfig());
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		
		ValidationConfig config = new ValidationConfig();
		config.setTestOnBorrow(true);
		ValidatablePoolListener<SpyObject> listener = new ValidatePoolEntryListener<SpyObject>(pool, config);
		
		int beforeValidatePermits = pool.availablePermits();
		PoolEntry<SpyObject> validatedEntry = listener.afterBorrowEntry(entry, true, 0);
		int afterValidatePermits = pool.availablePermits();
		
		assertNotNull(validatedEntry);
		assertEquals(beforeValidatePermits, afterValidatePermits);
	}

	@Test
	public void afterBorrowEntry_validation失敗の場合() throws Exception {
		PoolConfig poolConfig = new PoolConfig();
		
		boolean forceInvalid = true;
		PoolEntryFactory<SpyObject> entryFactory = 
				PoolTestUtil.createPoolEntryFactory(new SpyObjectFactory(), new SpyObjectValidator(forceInvalid));
		
		IdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class, poolConfig);
		
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(poolConfig, queue, entryFactory);
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		
		ValidationConfig validationConfig = new ValidationConfig();
		validationConfig.setTestOnBorrow(true);
		validationConfig.setTestOnReturn(false);
		ValidatablePoolListener<SpyObject> listener = new ValidatePoolEntryListener<SpyObject>(pool, validationConfig);
		
		int beforeValidatePermits = pool.availablePermits();
		try {
			listener.afterBorrowEntry(entry, true, 0);
		} catch (PoolException e) {
			int afterValidatePermits = pool.availablePermits();
			assertEquals(beforeValidatePermits + 1, afterValidatePermits);
			
			return;
		}
		fail();
	}
}	
