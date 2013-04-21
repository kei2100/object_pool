package com.github.kei2100.pool.basic;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.kei2100.pool.PoolConfig;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PooledObjectValidator;
import com.github.kei2100.pool.basic.BasicIdleEntriesQueue;
import com.github.kei2100.pool.basic.BasicPoolEntry;
import com.github.kei2100.pool.util.SpyObject;


public class BasicIdleEntriesQueueTest {
	
	@Test(expected = NullPointerException.class)
	public void offer_追加エントリがnullの場合() {
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class);
		queue.offer(null);
	}
	
	@Test
	public void offer_maxIdleEntries数を超えない_追加entryがvalidの場合() {
		PoolConfig config = new PoolConfig();
		config.setMaxIdleEntries(1);
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class, config);
		BasicPoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class);
		
		boolean offerSuccess = queue.offer(entry);
		
		assertTrue(offerSuccess);
		assertTrue(entry.getState().isValid());
	}

	@Test
	public void offer_maxIdleEntries数を超えない_追加entryがinvalidの場合() throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxIdleEntries(1);
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class, config);
		BasicPoolEntry<SpyObject> entry = PoolTestUtil.createPoolEntry(SpyObject.class);
		
		entry.invalidate();
		boolean offerSuccess = queue.offer(entry);
		
		assertFalse(offerSuccess);
	}

	@Test
	public void offer_maxIdleEntries数を超える場合() {
		PoolConfig config = new PoolConfig();
		config.setMaxIdleEntries(1);
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class, config);
		BasicPoolEntry<SpyObject> entry1 = PoolTestUtil.createPoolEntry(SpyObject.class);
		BasicPoolEntry<SpyObject> entry2 = PoolTestUtil.createPoolEntry(SpyObject.class);
		
		boolean offerSuccess1 = queue.offer(entry1);
		boolean offerSuccess2 = queue.offer(entry2);
		
		assertTrue(offerSuccess1);
		assertFalse(offerSuccess2);
		assertTrue(entry1.getState().isValid());
		assertFalse(entry2.getState().isValid());
	}

	@Test
	public void offer_maxIdleEntries数を超える_invalidateで例外発生の場合() {
		PoolConfig config = new PoolConfig();
		config.setMaxIdleEntries(1);
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class, config);
		PooledObjectValidator<SpyObject> validator = PoolTestUtil.createThrowExceptionValidator(SpyObject.class);
		
		BasicPoolEntry<SpyObject> entry1 = new BasicPoolEntry<SpyObject>(new SpyObject(), validator);
		BasicPoolEntry<SpyObject> entry2 = new BasicPoolEntry<SpyObject>(new SpyObject(), validator);
		
		boolean offerSuccess1 = queue.offer(entry1);
		boolean offerSuccess2 = queue.offer(entry2);
		
		assertTrue(offerSuccess1);
		assertFalse(offerSuccess2);
		assertTrue(entry1.getState().isValid());
		assertFalse(entry2.getState().isValid());
	}
	
	@Test
	public void poll_アイドルエントリが空の場合() {
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class);
		
		PoolEntry<SpyObject> actualObject = queue.poll();
		assertNull(actualObject);
	}
	
	@Test
	public void poll_アイドルエントリが空でない場合() {
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class);
		
		queue.offer(PoolTestUtil.createPoolEntry(SpyObject.class));
		
		PoolEntry<SpyObject> actualObject = queue.poll();
		assertNotNull(actualObject);
	}
	
	@Test
	public void offer_pool_マルチスレッドで繰り返す() throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxIdleEntries(5);	// 5 is common num with pool size.
		
		final BasicIdleEntriesQueue<SpyObject> queue =
				PoolTestUtil.createQueue(SpyObject.class, config);
		
		ExecutorService es = Executors.newFixedThreadPool(5);	// 5 is common num with maxIdleEntries.
		List<Future<PoolEntry<SpyObject>>> futures = new ArrayList<Future<PoolEntry<SpyObject>>>();
		
		for (int i = 0; i < 50; i++) {
			Future<PoolEntry<SpyObject>> future = es.submit(
				new Callable<PoolEntry<SpyObject>>() {
					@Override
					public PoolEntry<SpyObject> call() throws Exception {
						queue.offer(PoolTestUtil.createPoolEntry(SpyObject.class));
						TimeUnit.MILLISECONDS.sleep(1);
						return queue.poll();
					}
				}
			);
			futures.add(future);
		}
		
		try {
			for (Future<PoolEntry<SpyObject>> future : futures) {
				PoolEntry<SpyObject> entry = future.get();
				
				boolean actualValid = entry.getObject().isValid();
				assertTrue(actualValid);
			}
		} finally {
			es.shutdown();
		}
	}
}
