package com.jfirer.jfire.test.function.base.data;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.ContextAwareContextInited;

import javax.annotation.Resource;

@Resource
public class House implements ContextAwareContextInited
{
    private String          name;
    @Resource
    private ImmutablePerson host;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ImmutablePerson getHost()
    {
        return host;
    }

    public void setHost(ImmutablePerson host)
    {
        this.host = host;
    }

    @Override
    public void aware(ApplicationContext environment)
    {
        name = "林斌的房子";
    }
}
