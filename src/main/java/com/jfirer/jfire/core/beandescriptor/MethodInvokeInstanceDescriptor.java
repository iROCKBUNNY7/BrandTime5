package com.jfirer.jfire.core.beandescriptor;

import com.jfirer.jfire.core.beanfactory.DefaultMethodBeanFactory;

import java.lang.reflect.Method;

public class MethodInvokeInstanceDescriptor implements InstanceDescriptor
{
    private final Method method;

    public MethodInvokeInstanceDescriptor(Method method)
    {
        this.method = method;
    }

    @Override
    public Object newInstanceDescriptor()
    {
        return method;
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
