package com.jfireframework.jfire.core.aop.impl.transaction;

public interface TransactionManager
{
    
    /**
     * 开启事务.
     */
    TransactionState beginTransAction(int propagation);
    
    void commit(TransactionState transaction);
    
    void rollback(TransactionState transaction, Throwable e);
}
