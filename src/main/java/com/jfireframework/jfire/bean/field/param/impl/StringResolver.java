package com.jfireframework.jfire.bean.field.param.impl;

public class StringResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        this.value = value;
    }
    
}