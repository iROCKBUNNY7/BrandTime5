package com.jfireframework.jfire.core.aop.impl;

import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.aop.AopManager;

public class TransactionAopManager implements AopManager
{
    
    @Override
    public void scan(Environment environment)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void enhance(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void enhanceFinish(Class<?> type, Class<?> enhanceType, Environment environment)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void fillBean(Object bean, Class<?> type)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public int order()
    {
        return TRANSACTION;
    }
    
    public static interface TransactionManager
    {
        
        /**
         * 开启事务.
         */
        void beginTransAction();
        
        /**
         * 提交事务,但是并不关闭连接
         */
        void commit();
        
        /**
         * 事务回滚,但是并不关闭连接
         */
        void rollback(Throwable e);
        
    }
}
