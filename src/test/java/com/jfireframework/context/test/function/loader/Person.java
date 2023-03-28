package com.jfireframework.context.test.function.loader;

import com.jfireframework.jfire.core.beanfactory.SelectBeanFactory;

import javax.annotation.Resource;

@Resource
@SelectBeanFactory("allLoader")
public interface Person
{
    String getName();
}
