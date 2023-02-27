package com.jfireframework.jfire.core.aop.impl.transaction;

public class JdbcTransactionState implements TransactionState
{
    private int     propagation;
    private boolean beginWithNewConnection;
    private boolean rootTransaction;

    public JdbcTransactionState(int propagation, boolean beginWithNewConnection, boolean rootTransaction)
    {
        this.propagation = propagation;
        this.beginWithNewConnection = beginWithNewConnection;
        this.rootTransaction = rootTransaction;
    }

    @Override
    public int propagation()
    {
        return propagation;
    }

    @Override
    public boolean isContextCompleted()
    {
        return rootTransaction;
    }

    @Override
    public boolean isBeginWithNewConnection()
    {
        return beginWithNewConnection;
    }
}
