package com.jfireframework.jfire.bean.field.param.impl;

public class WBooleanResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        this.value = Boolean.valueOf(value);
    }
    
}