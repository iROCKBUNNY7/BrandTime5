package com.jfireframework.context.test.function.loader;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.kernel.Jfire;
import com.jfireframework.jfire.support.JfirePrepared.ComponentScan;
import com.jfireframework.jfire.support.JfirePrepared.configuration.Configuration;

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
        Jfire jfire = jfireConfig.build();
        Person person = jfire.getBean(Person.class);
        Assert.assertEquals("name", person.getName());
        Home home = jfire.getBean(Home.class);
        Assert.assertEquals(100, home.getLength());
    }
}
