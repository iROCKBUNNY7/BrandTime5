package com.jfireframework.context.test.function.loader;

import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.prepare.annotation.ComponentScan;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

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
        JfireBootstrap jfireConfig = new JfireBootstrap(HolderTestScan.class);
        Jfire          jfire       = jfireConfig.start();
        Person         person      = jfire.getBean(Person.class);
        Assert.assertEquals("name", person.getName());
        Home home = jfire.getBean(Home.class);
        Assert.assertEquals(100, home.getLength());
    }
}
