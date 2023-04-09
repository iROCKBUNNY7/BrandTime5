package com.jfirer.jfire.test.function.initmethod;

import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class InitMethodTest
{
    @Configuration
    @ComponentScan("com.jfirer.jfire.test.function.initmethod")
    public static class InitMethodTestScan
    {

    }

    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext(InitMethodTestScan.class);
        Person             person  = context.getBean(Person.class);
        Assert.assertEquals(23, person.getAge());
        Assert.assertEquals("林斌", person.getName());
    }
}
