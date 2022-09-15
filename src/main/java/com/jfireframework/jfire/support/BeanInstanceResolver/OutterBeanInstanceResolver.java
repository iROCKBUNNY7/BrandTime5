package com.jfireframework.jfire.support.BeanInstanceResolver;

import java.util.Map;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;
import com.jfireframework.jfire.kernel.Environment;

public class OutterBeanInstanceResolver implements BeanInstanceResolver
{
    private final String beanName;
    private final Object instance;
    
    public OutterBeanInstanceResolver(String beanName, Object instance)
    {
        this.beanName = beanName;
        this.instance = instance;
    }
    
    @Override
    public Object getInstance(Map<String, Object> beanInstanceMap)
    {
        beanInstanceMap.put(beanName, instance);
        return instance;
    }
    
    @Override
    public void initialize(Environment environment)
    {
        ;
    }
    
    @Override
    public void close()
    {
        ;
    }
}
