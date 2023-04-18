package com.jfirer.jfire.core.beandescriptor;

import com.jfirer.jfire.core.beanfactory.DefaultClassBeanFactory;

public class ClassReflectInstanceDescriptor implements InstanceDescriptor
{
    private final Class ckass;

    public ClassReflectInstanceDescriptor(Class ckass)
    {
        this.ckass = ckass;
    }

    @Override
    public Object newInstanceDescriptor()
    {
        return ckass;
    }

    @Override
    public String selectedBeanFactoryBeanName()
    {
        return null;
    }

    @Override
    public Class<?> selectedBeanFactoryBeanClass()
    {
        return DefaultClassBeanFactory.class;
    }
}
