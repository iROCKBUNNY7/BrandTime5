package com.jfireframework.context.test.function.validate;

import com.sun.istack.internal.NotNull;

public class User
{
    @NotNull
    private String name;
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
}
