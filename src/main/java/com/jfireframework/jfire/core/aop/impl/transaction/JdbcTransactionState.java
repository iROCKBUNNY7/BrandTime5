package com.jfireframework.jfire.core.aop.impl.transaction;

public class JdbcTransactionState implements TransactionState
{
    private final ConnectionHolder connection;
    private final boolean          needCommit;
    private final boolean          needClose;
    
    public JdbcTransactionState(ConnectionHolder connection, boolean needCommit, boolean needClose)
    {
        this.connection = connection;
        this.needCommit = needCommit;
        this.needClose = needClose;
    }
    
    @Override
    public void commit()
    {
        if (needCommit)
        {
            connection.commit();
        }
        if (needClose)
        {
            connection.close();
        }
    }
    
    @Override
    public void rollback(Throwable e)
    {
        if (needCommit)
        {
            connection.rollback();
        }
        if (needClose)
        {
            connection.close();
        }
    }
    
}
