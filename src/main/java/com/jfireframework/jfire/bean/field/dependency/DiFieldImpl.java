package com.jfireframework.jfire.bean.field.dependency;

import java.lang.reflect.Field;
import java.util.Map;

public class DiFieldImpl implements DIField
{
    private final DiResolver diResolver;
    private final Field      field;
    
    public DiFieldImpl(DiResolver diResolver, Field field)
    {
        this.diResolver = diResolver;
        this.field = field;
    }
    
    @Override
    public void inject(Object src, Map<String, Object> beanInstanceMap)
    {
        diResolver.inject(src, beanInstanceMap);
    }
    
    @Override
    public DiResolver diResolver()
    {
        return diResolver;
    }
    
    @Override
    public String name()
    {
        return field.getName();
    }
    
}
