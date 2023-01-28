package com.jfireframework.jfire.core.aop.impl.transaction;

public interface ConnectionHolder
{
    
    void beginTransaction();
    
    boolean isTransactionActive();
    
    void commit();
    
    void rollback();
    
    void close();
    
    boolean isClosed();
    
    /**
     * 获取上下文中前一个连接对象
     * 
     * @return
     */
    ConnectionHolder getPrev();
    
    void setPrev(ConnectionHolder connectionHolder);
}
