package com.github.kei2100.pool.util;

import java.util.concurrent.atomic.AtomicInteger;

public class SpyObject {
	
	private boolean valid = true;
	private long createdAt = System.currentTimeMillis();
	
	private AtomicInteger validateCallCount = new AtomicInteger(0);
	private AtomicInteger invalidateCallCount = new AtomicInteger(0);
	
	public boolean isValid() {
		return valid;
	}
	
	public void validate() {
		validateCallCount.incrementAndGet();
		this.valid = true;
	}
	
	public void invalidate() {
		invalidateCallCount.incrementAndGet();
		this.valid = false;
	}
	
	public long getCreatedAt() {
		return createdAt;
	}
	
	public int getValidateCallCount() {
		return validateCallCount.intValue();
	}
	
	public int getInvalidateCallCount() {
		return invalidateCallCount.intValue();
	}
}
