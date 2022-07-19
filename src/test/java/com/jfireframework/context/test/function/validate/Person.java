package com.jfireframework.context.test.function.validate;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Resource
public class Person
{
    public String sayHello(@Valid User user)
    {
        return "hello " + user.getName();
    }
    
    public String sayHello2(@NotNull String name)
    {
        return "hello";
    }
}
