package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param;

import java.lang.reflect.Field;

public class ParamFieldImpl implements ParamField
{
    private final ParamResolver resolver;
    private final Field              field;
    
    public ParamFieldImpl(Field field, ParamResolver resolver)
    {
        this.field = field;
        this.resolver = resolver;
    }
    
    @Override
    public void setParam(Object entity)
    {
        resolver.setValue(entity);
    }
    
    @Override
    public String getName()
    {
        return field.getName();
    }
    
    @Override
    public ParamResolver resolver()
    {
        return resolver;
    }
    
}
