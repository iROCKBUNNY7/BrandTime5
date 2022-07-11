package com.jfireframework.jfire.bean.impl;

import java.util.Map;

public class OuterEntityBean extends BaseBean
{
    
    public OuterEntityBean(String beanName, Object outterEntity)
    {
        super(outterEntity.getClass(), beanName, false, null, null, false);
        singletonInstance = outterEntity;
    }
    
    @Override
    public Object getInstance()
    {
        return singletonInstance;
    }
    
    @Override
    public Object getInstance(Map<String, Object> beanInstanceMap)
    {
        return singletonInstance;
    }
    
    @Override
    protected Object buildInstance(Map<String, Object> beanInstanceMap)
    {
        throw new UnsupportedOperationException();
    }
    
}
