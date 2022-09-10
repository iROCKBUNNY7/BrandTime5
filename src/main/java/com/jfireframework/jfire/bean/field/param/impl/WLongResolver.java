package com.jfireframework.jfire.bean.field.param.impl;

public class WLongResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        this.value = Long.valueOf(value);
    }
    
}