package com.jfirer.jfire.exception;

public class TransactionException extends RuntimeException
{
    /**
     *
     */
    private static final long serialVersionUID = 2151383699266127930L;

    public TransactionException(Throwable e)
    {
        super(e);
    }
}
