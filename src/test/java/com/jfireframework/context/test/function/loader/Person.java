package com.jfireframework.context.test.function.loader;

import javax.annotation.Resource;
import com.jfireframework.jfire.support.BeanInstanceResolver.LoadByBeanInstanceResolver.LoadBy;

@Resource
@LoadBy(factoryBeanName = "allLoader")
public interface Person
{
    public String getName();
}
