package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl;

import com.jfireframework.baseutil.exception.JustThrowException;

public class ClassResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        try
        {
            this.value = this.getClass().getClassLoader().loadClass(value);
        }
        catch (ClassNotFoundException e)
        {
            throw new JustThrowException(e);
        }
    }
}