package com.github.kei2100.pool;

import com.github.kei2100.pool.asyncadjust.AsyncAdjustIdleEntriesQueue;
import com.github.kei2100.pool.basic.BasicIdleEntriesQueue;
import com.github.kei2100.pool.basic.BasicPool;
import com.github.kei2100.pool.basic.BasicPoolEntryFactory;
import com.github.kei2100.pool.validatable.ValidatablePool;


/**
 * A factory class for {@link Pool}
 * 
 * @param <T> the type of included object in {@link PoolEntry}
 * */
public class PoolFactory<T> {
	
	private final PoolConfig poolConfig;
	private final ValidationConfig validationConfig;
	private final PooledObjectFactory<T> pooledObjectfactory;
	private final PooledObjectValidator<T> pooledObjectValidator;
	
	/**
	 * Constructor
	 * 
	 * @param poolConfig {@link PoolConfig}
	 * @param validationConfig {@link ValidationConfig}
	 * @param pooledObjectfactory {@link PooledObjectFactory}
	 * @param pooledObjectValidator {@link PooledObjectValidator}
	 * */
	public PoolFactory(
			PoolConfig poolConfig,
			ValidationConfig validationConfig,
			PooledObjectFactory<T> pooledObjectfactory,
			PooledObjectValidator<T> pooledObjectValidator)
	{
		this.poolConfig = poolConfig;
		this.validationConfig = validationConfig;
		this.pooledObjectfactory = pooledObjectfactory;
		this.pooledObjectValidator = pooledObjectValidator;
	}
	
	/**
	 * Create instance of {@link Pool}.
	 * @return instance of {@link Pool}
	 * */
	public Pool<T> createInstance() {
		poolConfig.validateConfig();
		validationConfig.validateConfig();
		
		PoolEntryFactory<T> poolEntryFactory = createPoolEntryFactory();
		IdleEntriesQueue<T> idleEntriesQueue = createIdleEntriesQueue(poolEntryFactory);
		Pool<T> pool = createPool(idleEntriesQueue, poolEntryFactory);
		
		return pool;
	}

	private PoolEntryFactory<T> createPoolEntryFactory() {
		return new BasicPoolEntryFactory<T>(pooledObjectfactory, pooledObjectValidator);
	}

	private IdleEntriesQueue<T> createIdleEntriesQueue(PoolEntryFactory<T> poolEntryFactory) {
		if (poolConfig.isInvalidateInBackground() || poolConfig.isInvalidateInBackground()) {
			return new AsyncAdjustIdleEntriesQueue<T>(poolConfig, poolEntryFactory);
		}
		return new BasicIdleEntriesQueue<T>(poolConfig);
	}
	
	private Pool<T> createPool(IdleEntriesQueue<T> idleEntriesQueue, PoolEntryFactory<T> poolEntryFactory) {
		Pool<T> delegate =
				new BasicPool<T>(poolConfig, idleEntriesQueue, poolEntryFactory);
		
		return new ValidatablePool<T>(delegate, validationConfig);
	}
}
