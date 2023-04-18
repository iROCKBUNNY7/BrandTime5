package com.jfirer.jfire.core.beandescriptor;

public interface InstanceDescriptor
{
    Object newInstanceDescriptor();

    String selectedBeanFactoryBeanName();

    Class<?> selectedBeanFactoryBeanClass();
}
