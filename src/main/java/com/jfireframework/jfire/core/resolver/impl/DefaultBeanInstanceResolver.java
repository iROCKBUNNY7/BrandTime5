package com.jfireframework.jfire.core.resolver.impl;

import com.jfireframework.jfire.core.EnvironmentTmp;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.exception.NewBeanInstanceException;

public class DefaultBeanInstanceResolver implements BeanInstanceResolver
{
    private final Class<?> type;

    public DefaultBeanInstanceResolver(Class<?> type)
    {
        this.type = type;
    }

    @Override
    public Object buildInstance()
    {
        try
        {
            return type.newInstance();
        } catch (Exception e)
        {
            throw new NewBeanInstanceException(e);
        }
    }

    @Override
    public void init(EnvironmentTmp environment)
    {
        // TODO Auto-generated method stub
    }
}
