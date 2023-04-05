package com.jfirer.jfire.test.function.loader;

import com.jfirer.jfire.core.beanfactory.SelectBeanFactory;

import javax.annotation.Resource;

@Resource
@SelectBeanFactory("allLoader")
public interface Person
{
    String getName();
}
