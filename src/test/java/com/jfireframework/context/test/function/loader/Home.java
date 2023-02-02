package com.jfireframework.context.test.function.loader;

import com.jfireframework.jfire.core.resolver.impl.LoadByBeanInstanceResolver.LoadBy;

import javax.annotation.Resource;

@Resource
@LoadBy(factoryBeanName = "allLoader")
public interface Home
{
    int getLength();
}
