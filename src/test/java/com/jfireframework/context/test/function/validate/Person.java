package com.jfireframework.context.test.function.validate;

import javax.annotation.Resource;
import com.jfireframework.jfire.validate.Validate;

@Resource
public class Person
{
    @Validate
    public String sayHello(User user)
    {
        return "hello " + user.getName();
    }
    
    public String sayHello2(@Validate User user)
    {
        return "hello " + user.getName();
    }
}
