package com.jfireframework.jfire.core.beandescriptor;

import java.lang.reflect.Method;

public interface BeanDescriptor
{

    Class<?> getDescriptorClass();

    Method getDescriptorMethod();

    String beanName();

    boolean isPrototype();

    String selectedBeanFactoryBeanName();

    Class<?> selectedBeanFactoryBeanClass();
}
