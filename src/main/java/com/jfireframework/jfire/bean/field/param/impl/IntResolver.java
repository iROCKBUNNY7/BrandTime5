package com.jfireframework.jfire.bean.field.param.impl;

import java.lang.reflect.Field;
import com.jfireframework.jfire.bean.field.param.ParamResolver;

public class IntResolver implements ParamResolver
{
    private int  value;
    private long offset;
    
    @Override
    public void setValue(Object entity)
    {
        unsafe.putInt(entity, offset, value);
    }
    
    @Override
    public void initialize(String value, Field field)
    {
        offset = unsafe.objectFieldOffset(field);
        this.value = Integer.parseInt(value);
    }
    
}