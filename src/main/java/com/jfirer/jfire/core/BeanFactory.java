package com.jfirer.jfire.core;

import com.jfirer.jfire.core.beandescriptor.BeanDescriptor;

public interface BeanFactory
{
    <E> E getBean(BeanDescriptor beanDescriptor);
}