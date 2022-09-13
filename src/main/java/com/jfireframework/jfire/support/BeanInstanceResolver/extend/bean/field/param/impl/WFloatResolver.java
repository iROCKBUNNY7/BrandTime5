package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl;

public class WFloatResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        this.value = Float.valueOf(value);
    }
    
}