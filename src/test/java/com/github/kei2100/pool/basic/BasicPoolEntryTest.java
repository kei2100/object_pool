package com.github.kei2100.pool.basic;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.kei2100.pool.basic.BasicPoolEntry;
import com.github.kei2100.pool.util.SpyObject;
import com.github.kei2100.pool.util.SpyObjectValidator;

import static org.junit.Assert.*;


public class BasicPoolEntryTest {
	
	@Test
	public void validate_entryが既にinvalidの場合() throws Exception {
		BasicPoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class);
		long beforeTest = entry.getState().getLastValidatedAt();
		
		entry.invalidate();
		TimeUnit.MILLISECONDS.sleep(1);
		
		boolean validateSuccessful = entry.validate();
		long afterTest = entry.getState().getLastValidatedAt();
		
		assertFalse(validateSuccessful);
		assertFalse(entry.getState().isValid());
		assertEquals(beforeTest, afterTest);
	}
	
	@Test
	public void validate_validatorでvalidになった場合() throws Exception {
		BasicPoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class);
		long beforeTest = entry.getState().getLastValidatedAt();
		
		TimeUnit.MILLISECONDS.sleep(1);
		
		boolean validateSuccessful = entry.validate();
		long afterTest = entry.getState().getLastValidatedAt();
		
		assertTrue(validateSuccessful);
		assertTrue(entry.getState().isValid());
		assertTrue(beforeTest < afterTest);		
	}

	@Test
	public void validate_validatorでinvalidになった場合() throws Exception {
		boolean forceInvalid = true;
		SpyObjectValidator validator = new SpyObjectValidator(forceInvalid);
		BasicPoolEntry<SpyObject> entry = new BasicPoolEntry<SpyObject>(new SpyObject(), validator);

		long beforeTest = entry.getState().getLastValidatedAt();
		
		TimeUnit.MILLISECONDS.sleep(1);
		
		boolean validateSuccessful = entry.validate();
		long afterTest = entry.getState().getLastValidatedAt();
		
		assertFalse(validateSuccessful);
		assertFalse(entry.getState().isValid());
		assertTrue(beforeTest == afterTest);		
	}
	
	@Test
	public void invalidate_一度invalidateが呼ばれた場合() throws Exception {
		SpyObjectValidator validator = new SpyObjectValidator();
		BasicPoolEntry<SpyObject> entry = new BasicPoolEntry<SpyObject>(new SpyObject(), validator);
		
		entry.invalidate();

		assertFalse(entry.getState().isValid());
		assertEquals(1, validator.getInvalidateCallCount());
	}

	@Test
	public void invalidate_二度invalidateが呼ばれた場合() throws Exception {
		SpyObjectValidator validator = new SpyObjectValidator();
		BasicPoolEntry<SpyObject> entry = new BasicPoolEntry<SpyObject>(new SpyObject(), validator);
		
		entry.invalidate();
		entry.invalidate();
		
		assertFalse(entry.getState().isValid());
		assertEquals(1, validator.getInvalidateCallCount());
	}
	
}
