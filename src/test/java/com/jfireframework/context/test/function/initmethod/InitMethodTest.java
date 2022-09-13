package com.jfireframework.context.test.function.initmethod;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.support.jfireprepared.ComponentScan;
import com.jfireframework.jfire.support.jfireprepared.Configuration;

public class InitMethodTest
{
    @Configuration
    @ComponentScan("com.jfireframework.context.test.function.initmethod")
    public static class InitMethodTestScan
    {
        
    }
    
    @Test
    public void test()
    {
        JfireConfig config = new JfireConfig(InitMethodTestScan.class);
        Person person = new Jfire(config).getBean(Person.class);
        Assert.assertEquals(23, person.getAge());
        Assert.assertEquals("林斌", person.getName());
    }
    
}
