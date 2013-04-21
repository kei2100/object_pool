package com.github.kei2100.pool;

public class PoolException extends Exception{

	private static final long serialVersionUID = 678424968631025325L;
	
	public PoolException() {
		super();
	}

	public PoolException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public PoolException(String arg0) {
		super(arg0);
	}

	public PoolException(Throwable arg0) {
		super(arg0);
	}
}
