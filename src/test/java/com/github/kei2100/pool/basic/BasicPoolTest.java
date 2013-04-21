package com.github.kei2100.pool.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.github.kei2100.pool.IdleEntriesQueue;
import com.github.kei2100.pool.PoolConfig;
import com.github.kei2100.pool.PoolEntry;
import com.github.kei2100.pool.PoolEntryFactory;
import com.github.kei2100.pool.PoolException;
import com.github.kei2100.pool.basic.BasicIdleEntriesQueue;
import com.github.kei2100.pool.basic.BasicPool;
import com.github.kei2100.pool.util.SpyObject;
import com.github.kei2100.pool.util.ThrowExceptionObjectFactory;


public class BasicPoolTest {
	
	@Test
	public void borrowEntry_maxActiveEntriesを超えない_initialEntriesを超えない場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(2);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config);
		
		TimeUnit.MILLISECONDS.sleep(1);
		long beforeExec = System.currentTimeMillis();
		TimeUnit.MILLISECONDS.sleep(1);
		
		PoolEntry<SpyObject> entry1 = pool.borrowEntry(1, TimeUnit.MILLISECONDS);
		PoolEntry<SpyObject> entry2 = pool.borrowEntry(1, TimeUnit.MILLISECONDS);
		
		assertTrue(entry1.getObject().getCreatedAt() < beforeExec);
		assertTrue(entry2.getObject().getCreatedAt() < beforeExec);
		assertEquals(0, pool.availablePermits());
	}
	
	@Test
	public void borrowEntry_maxActiveEntriesを超えない_initialEntriesを超える_createNewする場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(1);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config);
		
		TimeUnit.MILLISECONDS.sleep(1);
		long beforeExec = System.currentTimeMillis();
		TimeUnit.MILLISECONDS.sleep(1);
		
		boolean createNew = true;
		PoolEntry<SpyObject> entry1 = pool.borrowEntry(createNew, 1, TimeUnit.MILLISECONDS);
		PoolEntry<SpyObject> entry2 = pool.borrowEntry(createNew, 1, TimeUnit.MILLISECONDS);
		
		assertTrue(entry1.getObject().getCreatedAt() < beforeExec);
		assertTrue(entry2.getObject().getCreatedAt() > beforeExec);
		assertEquals(0, pool.availablePermits());
	}

	@Test
	public void borrowEntry_maxActiveEntriesを超えない_initialEntriesを超える_createNewしない場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(1);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config);
		
		boolean notCreateNew = false;
		PoolEntry<SpyObject> entry1 = pool.borrowEntry(notCreateNew, 1, TimeUnit.MILLISECONDS);
		PoolEntry<SpyObject> entry2 = pool.borrowEntry(notCreateNew, 1, TimeUnit.MILLISECONDS);
		
		assertNotNull(entry1);
		assertNull(entry2);
		assertEquals(1, pool.availablePermits());
	}

	@Test
	public void borrowEntry_maxActiveEntriesを超えない_initialEntriesを超える_createNewで例外の場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(1);

		PoolEntryFactory<SpyObject> entryFactory = 
				PoolTestUtil.createPoolEntryFactory(
						SpyObject.class, new ThrowExceptionObjectFactory<SpyObject>(SpyObject.class, 2));
		IdleEntriesQueue<SpyObject> queue = 
				PoolTestUtil.createQueue(SpyObject.class, config);
		
		BasicPool<SpyObject> pool = new BasicPool<SpyObject>(config, queue, entryFactory);
		assertNotNull(pool.borrowEntry(1, TimeUnit.MILLISECONDS));
		try {
			pool.borrowEntry(1, TimeUnit.MILLISECONDS);
		} catch (PoolException e) {
			assertEquals(1, pool.availablePermits());
			return;
		}
		fail();
	}
	
	@Test(expected = TimeoutException.class)
	public void borrowEntry_maxActiveEntriesを超える場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(2);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config);
		
		PoolEntry<SpyObject> entry1 = pool.borrowEntry(1, TimeUnit.MILLISECONDS);
		PoolEntry<SpyObject> entry2 = pool.borrowEntry(1, TimeUnit.MILLISECONDS);
		
		assertNotNull(entry1);
		assertNotNull(entry2);
		
		pool.borrowEntry(1, TimeUnit.MILLISECONDS);
	}

	@Test
	public void tryBorrowEntry_maxActiveEntriesを超えない_initialEntriesを超えない場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(2);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config);
		
		TimeUnit.MILLISECONDS.sleep(1);
		long beforeExec = System.currentTimeMillis();
		TimeUnit.MILLISECONDS.sleep(1);
		
		PoolEntry<SpyObject> entry1 = pool.tryBorrowEntry();
		PoolEntry<SpyObject> entry2 = pool.tryBorrowEntry();
		
		assertTrue(entry1.getObject().getCreatedAt() < beforeExec);
		assertTrue(entry2.getObject().getCreatedAt() < beforeExec);
		assertEquals(0, pool.availablePermits());
	}
	
	@Test
	public void tryBorrowEntry_maxActiveEntriesを超えない_initialEntriesを超える_createNewする場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(1);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config);
		
		TimeUnit.MILLISECONDS.sleep(1);
		long beforeExec = System.currentTimeMillis();
		TimeUnit.MILLISECONDS.sleep(1);
		
		boolean createNew = true;
		PoolEntry<SpyObject> entry1 = pool.tryBorrowEntry(createNew);
		PoolEntry<SpyObject> entry2 = pool.tryBorrowEntry(createNew);
		
		assertTrue(entry1.getObject().getCreatedAt() < beforeExec);
		assertTrue(entry2.getObject().getCreatedAt() > beforeExec);
		assertEquals(0, pool.availablePermits());
	}

	@Test
	public void tryBorrowEntry_maxActiveEntriesを超えない_initialEntriesを超える_createNewしない場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(1);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config);
		
		boolean notCreateNew = false;
		PoolEntry<SpyObject> entry1 = pool.tryBorrowEntry(notCreateNew);
		PoolEntry<SpyObject> entry2 = pool.tryBorrowEntry(notCreateNew);
		
		assertNotNull(entry1);
		assertNull(entry2);
		assertEquals(1, pool.availablePermits());
	}

	@Test
	public void tryBorrowEntry_maxActiveEntriesを超えない_initialEntriesを超える_createNewで例外の場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(1);

		PoolEntryFactory<SpyObject> entryFactory = 
				PoolTestUtil.createPoolEntryFactory(
						SpyObject.class, new ThrowExceptionObjectFactory<SpyObject>(SpyObject.class, 2));
		IdleEntriesQueue<SpyObject> queue = 
				PoolTestUtil.createQueue(SpyObject.class, config);
		
		BasicPool<SpyObject> pool = new BasicPool<SpyObject>(config, queue, entryFactory);
		assertNotNull(pool.tryBorrowEntry());
		try {
			pool.tryBorrowEntry();
		} catch (PoolException e) {
			assertEquals(1, pool.availablePermits());
			return;
		}
		fail();
	}
	
	@Test
	public void tryBorrowEntry_maxActiveEntriesを超える場合()
	throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(2);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config);
		
		PoolEntry<SpyObject> entry1 = pool.tryBorrowEntry();
		PoolEntry<SpyObject> entry2 = pool.tryBorrowEntry();
		PoolEntry<SpyObject> entry3 = pool.tryBorrowEntry();
		
		assertNotNull(entry1);
		assertNotNull(entry2);
		assertNull(entry3);
	}
	
	@Test
	public void returnEntry_returnするentryがvalidの場合() throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(2);
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class, config);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config, queue);
		
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		assertEquals(1, pool.availablePermits());
		assertEquals(1, queue.getIdleEntriesCount());
		
		pool.returnEntry(entry);
		assertEquals(2, pool.availablePermits());
		assertEquals(2, queue.getIdleEntriesCount());
	}

	@Test
	public void returnEntry_returnするentryがinvalidの場合() throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(2);
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class, config);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config, queue);
		
		PoolEntry<SpyObject> entry = pool.borrowEntry();
		assertEquals(1, pool.availablePermits());
		assertEquals(1, queue.getIdleEntriesCount());
		
		entry.invalidate();
		pool.returnEntry(entry);
		assertEquals(2, pool.availablePermits());
		assertEquals(1, queue.getIdleEntriesCount());
	}

	@Test
	public void returnEntry_returnするentryがnullの場合() throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(2);
		config.setMaxIdleEntries(2);
		config.setInitialEntries(2);
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class, config);
		BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config, queue);
		
		pool.borrowEntry();
		try {
			pool.returnEntry(null);
		} catch (NullPointerException e) {
			assertEquals(1, pool.availablePermits());
			assertEquals(1, queue.getIdleEntriesCount());
			return;
		}
		fail();
	}
	
	@Test
	public void borrow_return_マルチスレッドで繰り返す() throws Exception {
		PoolConfig config = new PoolConfig();
		config.setMaxActiveEntries(5);
		config.setMaxIdleEntries(5);
		config.setInitialEntries(5);
		BasicIdleEntriesQueue<SpyObject> queue = PoolTestUtil.createQueue(SpyObject.class, config);
		final BasicPool<SpyObject> pool = PoolTestUtil.createPool(SpyObject.class, config, queue);
		
		ExecutorService es = Executors.newFixedThreadPool(5);	// 5 is common num with maxIdleEntries.
		List<Future<Void>> futures = new ArrayList<Future<Void>>();
		
		for (int i = 0; i < 50; i++) {
			Future<Void> future = es.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					PoolEntry<SpyObject> entry = pool.tryBorrowEntry();
					TimeUnit.MILLISECONDS.sleep(1);
					pool.returnEntry(entry);
					return null;
				}
			});
			futures.add(future);
		}
		
		try {
			for (Future<Void> future : futures) {
				// exceptionが発生していないこと
				future.get();
			}
		} finally {
			es.shutdown();
		}
		
		assertEquals(5, pool.availablePermits());
		assertEquals(5, queue.getIdleEntriesCount());
	}
}
