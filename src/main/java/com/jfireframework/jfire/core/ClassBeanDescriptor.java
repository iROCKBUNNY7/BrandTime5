package com.jfireframework.jfire.core;

import java.lang.reflect.Method;

public class ClassBeanDescriptor implements BeanDescriptor
{
    private final Class<?> beanClass;
    private final String   beanName;
    private final boolean  prototype;

    public ClassBeanDescriptor(Class<?> beanClass, String beanName, boolean prototype)
    {
        this.beanClass = beanClass;
        this.beanName = beanName;
        this.prototype = prototype;
    }

    @Override
    public DescriptorType type()
    {
        return DescriptorType.CLASS;
    }

    @Override
    public Class<?> getDescriptorClass()
    {
        return beanClass;
    }

    @Override
    public Method getDescriptorMethod()
    {
        throw new IllegalStateException();
    }

    @Override
    public String beanName()
    {
        return beanName;
    }

    @Override
    public boolean isPrototype()
    {
        return prototype;
    }
}
