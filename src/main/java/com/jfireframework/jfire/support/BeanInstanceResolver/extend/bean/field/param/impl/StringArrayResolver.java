package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl;

public class StringArrayResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        this.value = value.split(",");
    }
}