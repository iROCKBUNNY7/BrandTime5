package com.jfireframework.jfire.core;

import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;

public interface BeanFactory
{
    <E> E getBean(BeanDescriptor beanDescriptor);
}