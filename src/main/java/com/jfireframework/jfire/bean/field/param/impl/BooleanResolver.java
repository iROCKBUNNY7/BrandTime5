package com.jfireframework.jfire.bean.field.param.impl;

import java.lang.reflect.Field;
import com.jfireframework.jfire.bean.field.param.ParamResolver;

public class BooleanResolver implements ParamResolver
{
    private long    offset;
    private boolean value;
    
    @Override
    public void setValue(Object entity)
    {
        unsafe.putBoolean(entity, offset, value);
    }
    
    @Override
    public void initialize(String value, Field field)
    {
        offset = unsafe.objectFieldOffset(field);
        this.value = Boolean.parseBoolean(value);
    }
}