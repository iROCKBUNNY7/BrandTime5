package com.jfireframework.jfire.core.beandescriptor;

import com.jfireframework.jfire.core.beanfactory.DefaultMethodBeanFactory;

import java.lang.reflect.Method;

public class MethodBeanDescriptor implements BeanDescriptor
{
    private final Class<?> beanClass;
    private final String   beanName;
    private final boolean  prototype;
    private final Method   method;

    public MethodBeanDescriptor(Method method, String beanName, boolean prototype)
    {
        this.method = method;
        this.beanClass = method.getReturnType();
        this.beanName = beanName;
        this.prototype = prototype;
    }

    @Override
    public Class<?> getDescriptorClass()
    {
        return null;
    }

    @Override
    public Method getDescriptorMethod()
    {
        return method;
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

    @Override
    public String selectedBeanFactoryBeanName()
    {
        return null;
    }

    @Override
    public Class<?> selectedBeanFactoryBeanClass()
    {
        return DefaultMethodBeanFactory.class;
    }
}
