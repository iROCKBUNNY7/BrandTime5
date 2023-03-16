package com.jfireframework.jfire.core;

import java.lang.reflect.Method;

public interface BeanDescriptor
{
    DescriptorType type();

    Class<?> getDescriptorClass();

    Method getDescriptorMethod();

    String beanName();

    boolean isPrototype();

    enum DescriptorType
    {
        METHOD, CLASS
    }
}
