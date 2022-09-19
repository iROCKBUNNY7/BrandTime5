package com.jfireframework.jfire.kernel;

import com.jfireframework.baseutil.exception.JustThrowException;

public class BeanDefinition
{
    private final Class<?>             type;
    private final BeanInstanceResolver beanInstanceResolver;
    private Object                     reflectInstance;
    
    public BeanDefinition(Class<?> type, BeanInstanceResolver beanInstanceResolver)
    {
        this.type = type;
        this.beanInstanceResolver = beanInstanceResolver;
    }
    
    public BeanInstanceResolver getBeanInstanceResolver()
    {
        return beanInstanceResolver;
    }
    
    public String getBeanName()
    {
        return beanInstanceResolver.beanName();
    }
    
    public Class<?> getType()
    {
        return type;
    }
    
    public boolean isPrototype()
    {
        return beanInstanceResolver.isPrototype();
    }
    
    public Object getReflectInstance()
    {
        if (reflectInstance == null)
        {
            try
            {
                reflectInstance = type.newInstance();
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        return reflectInstance;
    }
    
}
