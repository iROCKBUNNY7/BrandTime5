package com.jfirer.jfire.core;

import com.jfirer.jfire.core.beandescriptor.InstanceDescriptor;

public interface BeanFactory
{
    <E> E getInstance(InstanceDescriptor beanDescriptor);
}