package com.github.kei2100.pool.validatable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PooledObjectValidator;
import com.github.kei2100.pool.ValidationConfig;
import com.github.kei2100.pool.basic.PoolTestUtil;
import com.github.kei2100.pool.util.SpyObject;
import com.github.kei2100.pool.util.SpyObjectValidator;
import com.github.kei2100.pool.util.ThrowExceptionValidator;
import com.github.kei2100.pool.validatable.ValidationHelper;


public class ValidationHelperTest {
	
	@Test
	public void validate_testWithIntervalがtrue_intervalが経過していない場合() {
		SpyObjectValidator validator = new SpyObjectValidator();
		PoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class, validator);
		
		ValidationConfig config = new ValidationConfig();
		config.setTestIntervalMillis(300000);
		
		boolean validateSuccessful = ValidationHelper.validate(config, entry);
		int validateCallCount = validator.getValidateCallCount();
		
		assertTrue(validateSuccessful);
		assertEquals(0, validateCallCount);
	}

	@Test
	public void validate_testWithIntervalがtrue_intervalが経過している場合() {
		SpyObjectValidator validator = new SpyObjectValidator();
		PoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class, validator);
		
		ValidationConfig config = new ValidationConfig();
		config.setTestIntervalMillis(100);
		
		entry.getState().setLastValidatedAt(System.currentTimeMillis() - 101);
		boolean validateSuccessful = ValidationHelper.validate(config, entry);
		int validateCallCount = validator.getValidateCallCount();
		
		assertTrue(validateSuccessful);
		assertEquals(1, validateCallCount);
	}
	
	@Test
	public void validate_testWithIntervalがfalse_intervalが経過していない場合() {
		SpyObjectValidator validator = new SpyObjectValidator();
		PoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class, validator);
		
		ValidationConfig config = new ValidationConfig();
		
		boolean validateSuccessful = ValidationHelper.validate(config, entry);
		int validateCallCount = validator.getValidateCallCount();
		
		assertTrue(validateSuccessful);
		assertEquals(1, validateCallCount);
	}
	
	@Test
	public void validate_validateで失敗の場合() {
		boolean forceInvalidate = true;
		SpyObjectValidator validator = new SpyObjectValidator(forceInvalidate);
		PoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class, validator);
		
		ValidationConfig config = new ValidationConfig();
		
		boolean validateSuccessful = ValidationHelper.validate(config, entry);
		boolean isValid = entry.getState().isValid();
		
		assertFalse(validateSuccessful);
		assertFalse(isValid);
	}

	@Test
	public void validate_validateで例外の場合() {
		PooledObjectValidator<SpyObject> validator = 
				new ThrowExceptionValidator<SpyObject>(new SpyObjectValidator());
		PoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class, validator);
		
		ValidationConfig config = new ValidationConfig();
		
		boolean validateSuccessful = ValidationHelper.validate(config, entry);
		boolean isValid = entry.getState().isValid();
		
		assertFalse(validateSuccessful);
		assertFalse(isValid);
	}
	
	@Test
	public void invalidateIfAgeExpired_isMaxAgeUnlimitの場合() {
		SpyObjectValidator validator = new SpyObjectValidator();
		PoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class, validator);
		
		ValidationConfig config = new ValidationConfig();
		
		ValidationHelper.invalidateIfAgeExpired(config, entry);
		boolean isValid = entry.getState().isValid();
		int invalidateCallCount = validator.getInvalidateCallCount();

		assertTrue(isValid);
		assertEquals(0, invalidateCallCount);
	}

	@Test
	public void invalidateIfAgeExpired_isMaxAgeUnlimitでない_hasAgeExpiredしていない場合() {
		SpyObjectValidator validator = new SpyObjectValidator();
		PoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class, validator);
		
		ValidationConfig config = new ValidationConfig();
		config.setMaxAgeMillis(300000);
		
		ValidationHelper.invalidateIfAgeExpired(config, entry);
		boolean isValid = entry.getState().isValid();
		int invalidateCallCount = validator.getInvalidateCallCount();
		
		assertTrue(isValid);
		assertEquals(0, invalidateCallCount);
	}

	@Test
	public void invalidateIfAgeExpired_isMaxAgeUnlimitでない_hasAgeExpiredしている_invalidate成功の場合()
	throws Exception {
		SpyObjectValidator validator = new SpyObjectValidator();
		PoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class, validator);
		
		ValidationConfig config = new ValidationConfig();
		config.setMaxAgeMillis(1);
		TimeUnit.MILLISECONDS.sleep(2);
		
		ValidationHelper.invalidateIfAgeExpired(config, entry);
		boolean isValid = entry.getState().isValid();
		int invalidateCallCount = validator.getInvalidateCallCount();
		
		assertFalse(isValid);
		assertEquals(1, invalidateCallCount);
	}

	@Test
	public void invalidateIfAgeExpired_isMaxAgeUnlimitでない_hasAgeExpiredしている_invalidateで例外の場合()
	throws Exception {
		PooledObjectValidator<SpyObject> validator = new ThrowExceptionValidator<SpyObject>(new SpyObjectValidator());
		PoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class, validator);
		
		ValidationConfig config = new ValidationConfig();
		config.setMaxAgeMillis(1);
		TimeUnit.MILLISECONDS.sleep(2);
		
		ValidationHelper.invalidateIfAgeExpired(config, entry);
		boolean isValid = entry.getState().isValid();
		
		assertFalse(isValid);
	}
}
