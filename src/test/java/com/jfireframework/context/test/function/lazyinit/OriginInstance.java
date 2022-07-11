package com.jfireframework.context.test.function.lazyinit;

import javax.annotation.PostConstruct;
import com.jfireframework.jfire.bean.annotation.LazyInitUniltFirstInvoke;

@LazyInitUniltFirstInvoke
public class OriginInstance
{
    @PostConstruct
    public void init()
    {
        LazyInitTest.invokedCount += 1;
    }
    
    public String getName()
    {
        return "1234";
    }
}
