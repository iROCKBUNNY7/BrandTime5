package com.jfireframework.context.test.function.loader;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.support.jfireprepared.ComponentScan;
import com.jfireframework.jfire.support.jfireprepared.Configuration;

public class HolderTest
{
    @Configuration
    @ComponentScan("com.jfireframework.context.test.function.loader")
    public static class HolderTestScan
    {
        
    }
    
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig(HolderTestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        Assert.assertEquals("name", person.getName());
        Home home = jfire.getBean(Home.class);
        Assert.assertEquals(100, home.getLength());
    }
}
