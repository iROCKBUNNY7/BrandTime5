package com.jfireframework.jfire.core.aop.impl.transaction;

public interface TransactionState
{
    /**
     * 提交当前TransactionState实例打开的数据库事务，如果存在的话<br/>
     * 如果创建TransactionState实例时伴随着数据库连接的申请，则关闭连接
     */
    void commit();
    
    /**
     * 回滚当前TransactionState实例打开的事务，如果存在的话<br/>
     * 如果创建TransactionState实例时伴随着数据库连接的申请，则关闭连接
     * 
     */
    void rollback(Throwable e);
    
}
