package com.jfirer.jfire.test.function.loader;

import com.jfirer.jfire.core.AnnotatedApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class HolderTest
{
    @Configuration
    @ComponentScan("com.jfirer.jfire.test.function.loader")
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
