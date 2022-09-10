package com.jfireframework.jfire.bean.field.param.impl;

import java.lang.reflect.Field;
import com.jfireframework.jfire.bean.field.param.ParamResolver;

public class LongResolver implements ParamResolver
{
    private long value;
    private long offset;
    
    @Override
    public void setValue(Object entity)
    {
        unsafe.putLong(entity, offset, value);
    }
    
    @Override
    public void initialize(String value, Field field)
    {
        offset = unsafe.objectFieldOffset(field);
        this.value = Long.parseLong(value);
    }
}