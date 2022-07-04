package com.jfireframework.jfire.bean.field.dependency;

import java.lang.reflect.Field;
import com.jfireframework.jfire.bean.BeanDefinition;
import sun.reflect.MethodAccessor;

public class DIFieldInfo
{
    public static final int  NONE          = -1;
    public static final int  DEFAULT       = 1;
    public static final int  LIST          = 2;
    public static final int  BEAN_NAME_MAP = 3;
    public static final int  METHOD_MAP    = 4;
    private final Field      field;
    private final int        mode;
    private BeanDefinition   beanDefinition;
    private BeanDefinition[] beanDefinitions;
    private MethodAccessor   method_map_method;
    private Object[]         value_map_values;
    private final String     name;
    
    public DIFieldInfo(Field field, int mode)
    {
        this.field = field;
        this.mode = mode;
        name = field.getName();
    }
    
    public String getFieldName()
    {
        return name;
    }
    
    public BeanDefinition getBeanDefinition()
    {
        return beanDefinition;
    }
    
    public BeanDefinition[] getBeanDefinitions()
    {
        return beanDefinitions;
    }
    
    public void setBeanDefinition(BeanDefinition beanDefinition)
    {
        this.beanDefinition = beanDefinition;
    }
    
    public int mode()
    {
        return mode;
    }
    
    public void setBeanDefinitions(BeanDefinition[] beanDefinitions)
    {
        this.beanDefinitions = beanDefinitions;
    }
    
    public void setMethod_map_method(MethodAccessor method_map_method)
    {
        this.method_map_method = method_map_method;
    }
    
    public void setValue_map_values(Object[] value_map_values)
    {
        this.value_map_values = value_map_values;
    }
    
    public Field getField()
    {
        return field;
    }
    
    public MethodAccessor getMethod_map_method()
    {
        return method_map_method;
    }
    
    public Object[] getValue_map_values()
    {
        return value_map_values;
    }
    
}
