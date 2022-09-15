package com.jfireframework.jfire.kernel;

import com.jfireframework.baseutil.exception.JustThrowException;

public class BeanDefinition
{
    private final String               beanName;
    private final Class<?>             type;
    private final boolean              prototype;
    private final BeanInstanceResolver beanInstanceResolver;
    private Object                     reflectInstance;
    
    public BeanDefinition(String beanName, Class<?> type, boolean prototype, BeanInstanceResolver beanInstanceResolver)
    {
        this.beanName = beanName;
        this.type = type;
        this.prototype = prototype;
        this.beanInstanceResolver = beanInstanceResolver;
    }
    
    public BeanInstanceResolver getBeanInstanceResolver()
    {
        return beanInstanceResolver;
    }
    
    public String getBeanName()
    {
        return beanName;
    }
    
    public Class<?> getType()
    {
        return type;
    }
    
    public boolean isPrototype()
    {
        return prototype;
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
