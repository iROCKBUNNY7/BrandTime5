package com.jfireframework.context.test.function.aop;

import javax.annotation.Resource;

@Resource
public class Home
{
    private String name = "home";

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
