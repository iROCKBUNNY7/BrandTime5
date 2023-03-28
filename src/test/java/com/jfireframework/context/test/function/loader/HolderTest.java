package com.jfireframework.context.test.function.loader;

import com.jfireframework.jfire.core.AnnotatedApplicationContext;
import com.jfireframework.jfire.core.ApplicationContext;
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
        ApplicationContext context = new AnnotatedApplicationContext(HolderTestScan.class);
        Person             person  = context.getBean(Person.class);
        Assert.assertEquals("name", person.getName());
        Home home = context.getBean(Home.class);
        Assert.assertEquals(100, home.getLength());
    }
}
