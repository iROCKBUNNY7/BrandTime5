package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl;

public class IntegerResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        this.value = Integer.valueOf(value);
    }
    
}