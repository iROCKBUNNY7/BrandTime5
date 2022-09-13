package com.jfireframework.jfire.support.BeanInstanceResolver;

import java.util.Map;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;

public class OutterBeanInstanceResolver implements BeanInstanceResolver
{
    private final String   beanName;
    private final Class<?> type;
    private final Object   instance;
    
    public OutterBeanInstanceResolver(String beanName, Class<?> type, Object instance)
    {
        this.beanName = beanName;
        this.type = type;
        this.instance = instance;
    }
    
    @Override
    public Object getInstance(Map<String, Object> beanInstanceMap)
    {
        beanInstanceMap.put(beanName, instance);
        return instance;
    }
    
    @Override
    public void initialize(Map<String, BeanDefinition> definitions)
    {
        ;
    }
    
    @Override
    public void close()
    {
        ;
    }
    
    @Override
    public String beanName()
    {
        return beanName;
    }
    
    @Override
    public Class<?> beanType()
    {
        return type;
    }
    
}
