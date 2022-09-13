package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl;

public class WLongResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        this.value = Long.valueOf(value);
    }
    
}