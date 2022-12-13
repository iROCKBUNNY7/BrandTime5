package com.jfireframework.jfire.core.aop.impl;

import java.lang.reflect.Method;
import java.util.List;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.aop.AopManager;
import com.jfireframework.jfire.core.aop.notated.Transaction;

public class TransactionAopManager implements AopManager
{
    private BeanDefinition transactionBeandefinition;
    
    @Override
    public void scan(Environment environment)
    {
        for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
        {
            for (Method method : beanDefinition.getType().getMethods())
            {
                if (Utils.ANNOTATION_UTIL.isPresent(Transaction.class, method))
                {
                    beanDefinition.addAopManager(this);
                    break;
                }
            }
        }
    }
    
    @Override
    public void enhance(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName)
    {
        List<BeanDefinition> list = environment.getBeanDefinitionByAbstract(TransactionManager.class);
        if (list.size() == 0)
        {
            return;
        }
        transactionBeandefinition = list.get(0);
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
