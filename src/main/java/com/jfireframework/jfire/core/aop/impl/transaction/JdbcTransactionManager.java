package com.jfireframework.jfire.core.aop.impl.transaction;

public abstract class JdbcTransactionManager implements TransactionManager
{
    
    public static ThreadLocal<ConnectionHolder> CONTEXT = new ThreadLocal<ConnectionHolder>();
    
    @Override
    public TransactionState beginTransAction(int propagation)
    {
        switch (propagation)
        {
            case Propagation.REQUIRED:
            {
                ConnectionHolder connection = CONTEXT.get();
                if (connection == null)
                {
                    connection = getConnection(false);
                    CONTEXT.set(connection);
                    connection.beginTransaction();
                    JdbcTransactionState transactionState = new JdbcTransactionState(connection, true, true);
                    return transactionState;
                }
                if (connection.isTransactionActive())
                {
                    JdbcTransactionState transactionState = new JdbcTransactionState(connection, false, false);
                    return transactionState;
                }
                else
                {
                    connection.beginTransaction();
                    JdbcTransactionState transactionState = new JdbcTransactionState(connection, true, false);
                    return transactionState;
                }
            }
            case Propagation.SUPPORTS:
            {
                ConnectionHolder connection = CONTEXT.get();
                if (connection == null)
                {
                    connection = getConnection(false);
                    CONTEXT.set(connection);
                    JdbcTransactionState jdbcTransactionState = new JdbcTransactionState(connection, false, true);
                    return jdbcTransactionState;
                }
                return new JdbcTransactionState(connection, false, false);
            }
            case Propagation.MANDATORY:
            {
                ConnectionHolder connection = CONTEXT.get();
                if (connection == null || connection.isTransactionActive() == false)
                {
                    throw new IllegalStateException("当前上下文内没有事务");
                }
                return new JdbcTransactionState(connection, false, false);
            }
            case Propagation.REQUIRES_NEW:
                ConnectionHolder prev = CONTEXT.get();
                ConnectionHolder newConnection = getConnection(true);
                CONTEXT.set(newConnection);
                newConnection.setPrev(prev);
                return new JdbcTransactionState(newConnection, true, true);
            default:
                throw new IllegalArgumentException();
        }
    }
    
    /**
     * 获取一个连接。如果强制新连接为真，则必须开启一个新连接，否则可以重用当前上下文的连接
     * 
     * @param forceNew
     * @return
     */
    protected abstract ConnectionHolder getConnection(boolean forceNew);
    
    @Override
    public void commit(TransactionState transaction)
    {
        transaction.commit();
        closeCurrentConnectionIfNeed();
    }
    
    @Override
    public void rollback(TransactionState transaction, Throwable e)
    {
        transaction.rollback(e);
        closeCurrentConnectionIfNeed();
    }
    
    private void closeCurrentConnectionIfNeed()
    {
        ConnectionHolder connectionHolder = CONTEXT.get();
        if (connectionHolder.isClosed())
        {
            ConnectionHolder prev = connectionHolder.getPrev();
            if (prev == null)
            {
                CONTEXT.remove();
            }
            else
            {
                CONTEXT.set(prev);
            }
        }
    }
    
}
