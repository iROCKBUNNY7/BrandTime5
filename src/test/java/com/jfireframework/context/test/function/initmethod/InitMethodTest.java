package com.jfireframework.context.test.function.initmethod;

import com.jfireframework.jfire.core.AnnotatedApplicationContext;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.prepare.annotation.ComponentScan;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

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
        ApplicationContext context = new AnnotatedApplicationContext(InitMethodTestScan.class);
        Person             person  = context.getBean(Person.class);
        Assert.assertEquals(23, person.getAge());
        Assert.assertEquals("林斌", person.getName());
    }
}
