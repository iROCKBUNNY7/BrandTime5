package com.jfirer.jfire.core.beandescriptor;

import java.lang.reflect.Method;

public class ClassBeanDescriptor implements BeanDescriptor
{
    private final Class<?> beanClass;
    private final String   beanName;
    private final boolean  prototype;
    private       String   selectedBeanFactoryBeanName;
    private       Class<?> selectedBeanFactoryBeanClass;

    public ClassBeanDescriptor(Class<?> beanClass, String beanName, boolean prototype, Class<?> selectedBeanFactoryBeanClass)
    {
        this.beanClass = beanClass;
        this.beanName = beanName;
        this.prototype = prototype;
        this.selectedBeanFactoryBeanClass = selectedBeanFactoryBeanClass;
    }

    public ClassBeanDescriptor(Class<?> beanClass, String beanName, boolean prototype, String selectedBeanFactoryBeanName)
    {
        this.beanClass = beanClass;
        this.beanName = beanName;
        this.prototype = prototype;
        this.selectedBeanFactoryBeanName = selectedBeanFactoryBeanName;
    }

    @Override
    public Class<?> getDescriptorClass()
    {
        return beanClass;
    }

    @Override
    public Method getDescriptorMethod()
    {
        return null;
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
        return selectedBeanFactoryBeanName;
    }

    @Override
    public Class<?> selectedBeanFactoryBeanClass()
    {
        return selectedBeanFactoryBeanClass;
    }
}
