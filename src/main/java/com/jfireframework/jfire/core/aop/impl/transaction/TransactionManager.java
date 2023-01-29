package com.jfireframework.jfire.core.aop.impl.transaction;

public interface TransactionManager
{
    
    /**
     * 以指定的传播级别开启事务。如果当前上下文中不存在连接，则创建一个连接
     */
    TransactionState beginTransAction(int propagation);
    
    /**
     * 如果允许的话，提交事务。提交时如有必要，关闭当前连接
     * 
     * @param transaction
     */
    void commit(TransactionState transaction);
    
    /**
     * 如果允许的话，回滚事务。回滚时如有必要，关闭当前连接
     * 
     * @param transaction
     * @param e
     */
    void rollback(TransactionState transaction, Throwable e);
}
