package com.jfireframework.context.test.function.validate;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ValidateOnExecution;

@Resource
public class Person
{
	@ValidateOnExecution
    public String sayHello(@Valid User user)
    {
        return "hello " + user.getName();
    }
    
	@ValidateOnExecution
    public String sayHello2(@NotNull String name)
    {
        return "hello";
    }
}
