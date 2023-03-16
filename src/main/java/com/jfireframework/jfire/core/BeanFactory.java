package com.jfireframework.jfire.core;

public interface BeanFactory
{
    <E> E getBean(BeanDescriptor beanDescriptor);

    boolean match(BeanDescriptor beanDescriptor);
}
