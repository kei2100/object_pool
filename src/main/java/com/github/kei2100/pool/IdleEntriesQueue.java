package com.github.kei2100.pool;

/**
 * Queue for Idle {@link PoolEntry} interface.
 * 
 * @param <T> the type of included object in {@link PoolEntry}  
 * */
public interface IdleEntriesQueue<T> {
	
	/**
	 * Retrieves and removes the head of this queue, 
	 * or returns null if this queue is empty.
	 * 
	 * @return the head of this queue, or null if this queue is empty
	 * */
	PoolEntry<T> poll();
	
	/**
	 * Inserts the specified element at the tail of this queue 
	 * if it is possible to do so immediately without exceeding the queue's capacity, 
	 * returning true upon success and false if this queue is full.
	 * 
	 * Also, if entry to add is invalid, entry is not insert this queue.   
	 * 
	 * @param entry the element to add
	 * @return if the element was added to this queue, else false
	 * @throws NullPointerException if the specified element is null
	 * */
	boolean offer(PoolEntry<T> entry) throws NullPointerException;
}
