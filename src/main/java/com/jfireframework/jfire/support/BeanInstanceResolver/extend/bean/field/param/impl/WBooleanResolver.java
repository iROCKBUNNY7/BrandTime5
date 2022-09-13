package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl;

public class WBooleanResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        this.value = Boolean.valueOf(value);
    }
    
}