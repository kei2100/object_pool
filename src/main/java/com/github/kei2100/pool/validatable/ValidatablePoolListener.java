package com.github.kei2100.pool.validatable;

import java.util.concurrent.TimeoutException;

import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolException;


interface ValidatablePoolListener<T> {

	PoolEntry<T> afterBorrowEntry(PoolEntry<T> entry, boolean createNew, long elapsedMillis)
			throws InterruptedException, TimeoutException, PoolException;

	PoolEntry<T> afterTryBorrowEntry(PoolEntry<T> entry, boolean createNew) 
			throws PoolException;

	PoolEntry<T> beforeReturnEntry(PoolEntry<T> entry);

}