package com.jfireframework.jfire.exception;

public class PostConstructMethodException extends RuntimeException
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 308595552667041634L;
	
	public PostConstructMethodException(Exception e)
	{
		super(e);
	}
}
