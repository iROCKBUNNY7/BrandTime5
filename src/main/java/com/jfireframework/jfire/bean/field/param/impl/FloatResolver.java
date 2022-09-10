package com.jfireframework.jfire.bean.field.param.impl;

import java.lang.reflect.Field;
import com.jfireframework.jfire.bean.field.param.ParamResolver;

public class FloatResolver implements ParamResolver
{
    
    private long  offset;
    private float value;
    
    @Override
    public void setValue(Object entity)
    {
        unsafe.putFloat(entity, offset, value);
    }
    
    @Override
    public void initialize(String value, Field field)
    {
        offset = unsafe.objectFieldOffset(field);
        this.value = Float.parseFloat(value);
    }
    
}