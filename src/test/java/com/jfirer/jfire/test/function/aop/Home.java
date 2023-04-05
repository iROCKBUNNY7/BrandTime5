package com.jfirer.jfire.test.function.aop;

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
