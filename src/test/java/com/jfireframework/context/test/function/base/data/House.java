package com.jfireframework.context.test.function.base.data;

import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.JfireAwareContextInited;

import javax.annotation.Resource;

@Resource
public class House implements JfireAwareContextInited
{
    private String name;
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
    public void awareContextInited(ReadOnlyEnvironment environment)
    {
        name = "林斌的房子";
    }

}
