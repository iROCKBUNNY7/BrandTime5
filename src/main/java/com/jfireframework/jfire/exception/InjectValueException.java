package com.jfireframework.jfire.exception;

public class InjectValueException extends RuntimeException
{
    /**
     *
     */
    private static final long serialVersionUID = 5016598777557062959L;

    public InjectValueException(Throwable e)
    {
        super(e);
    }

    public InjectValueException(String msg)
    {
        super(msg);
    }
}
