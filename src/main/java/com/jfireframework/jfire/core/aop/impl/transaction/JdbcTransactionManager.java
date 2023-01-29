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
                    connection = openConnection();
                    CONTEXT.set(connection);
                    connection.beginTransaction();
                    JdbcTransactionState transactionState = new JdbcTransactionState(Propagation.REQUIRED, true, true);
                    return transactionState;
                }
                if (connection.isTransactionActive())
                {
                    JdbcTransactionState transactionState = new JdbcTransactionState(Propagation.REQUIRED, false, false);
                    return transactionState;
                }
                else
                {
                    connection.beginTransaction();
                    JdbcTransactionState transactionState = new JdbcTransactionState(Propagation.REQUIRED, false, true);
                    return transactionState;
                }
            }
            case Propagation.SUPPORTS:
            {
                ConnectionHolder connection = CONTEXT.get();
                if (connection == null)
                {
                    connection = openConnection();
                    CONTEXT.set(connection);
                    JdbcTransactionState jdbcTransactionState = new JdbcTransactionState(Propagation.SUPPORTS, true, false);
                    return jdbcTransactionState;
                }
                return new JdbcTransactionState(Propagation.SUPPORTS, false, false);
            }
            case Propagation.MANDATORY:
            {
                ConnectionHolder connection = CONTEXT.get();
                if (connection == null || connection.isTransactionActive() == false)
                {
                    throw new IllegalStateException("当前上下文内没有事务");
                }
                return new JdbcTransactionState(Propagation.MANDATORY, false, false);
            }
            case Propagation.REQUIRES_NEW:
                ConnectionHolder prev = CONTEXT.get();
                ConnectionHolder newConnection = openConnection();
                CONTEXT.set(newConnection);
                newConnection.setPrev(prev);
                return new JdbcTransactionState(Propagation.REQUIRES_NEW, true, true);
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
    protected abstract ConnectionHolder openConnection();
    
    @Override
    public void commit(TransactionState state)
    {
        if (state.isCompleted())
        {
            ConnectionHolder connectionHolder = CONTEXT.get();
            connectionHolder.commit();
        }
        if (state.isBeginWithNewConnection())
        {
            closeCurrentConnection(state);
        }
    }
    
    @Override
    public void rollback(TransactionState state, Throwable e)
    {
        if (state.isCompleted())
        {
            ConnectionHolder connectionHolder = CONTEXT.get();
            connectionHolder.rollback();
        }
        if (state.isBeginWithNewConnection())
        {
            closeCurrentConnection(state);
        }
        
    }
    
    private void closeCurrentConnection(TransactionState state)
    {
        ConnectionHolder connectionHolder = CONTEXT.get();
        connectionHolder.close();
        if (state.propagation() == Propagation.REQUIRES_NEW)
        {
            ConnectionHolder prev = connectionHolder.getPrev();
            CONTEXT.set(prev);
        }
    }
    
}
