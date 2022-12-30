package com.jfireframework.context.test.function.loader;

import javax.annotation.Resource;
import com.jfireframework.jfire.core.resolver.impl.LoadByBeanInstanceResolver.LoadBy;

@Resource
@LoadBy(factoryBeanName = "allLoader")
public interface Home
{
    public int getLength();
}
