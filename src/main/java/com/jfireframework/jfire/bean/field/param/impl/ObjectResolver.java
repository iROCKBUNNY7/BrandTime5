package com.jfireframework.jfire.bean.field.param.impl;

import java.lang.reflect.Field;
import com.jfireframework.jfire.bean.field.param.ParamResolver;

public abstract class ObjectResolver implements ParamResolver
{
    protected Object value;
    protected long   offset;
    protected Field  field;
    
    @Override
    public void setValue(Object entity)
    {
        unsafe.putObject(entity, offset, value);
    }
    
    @Override
    public void initialize(String value, Field field)
    {
        this.field = field;
        offset = unsafe.objectFieldOffset(field);
        initialize(value);
    }
    
    protected abstract void initialize(String value);
}
