package com.github.kei2100.pool.util;

import com.github.kei2100.pool.PooledObjectFactory;

public class SpyObjectFactory implements PooledObjectFactory<SpyObject> {
	@Override
	public SpyObject createInstance() throws Exception {
		return new SpyObject();
	}
}
