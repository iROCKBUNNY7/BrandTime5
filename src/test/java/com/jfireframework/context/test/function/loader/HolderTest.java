package com.jfireframework.context.test.function.loader;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;

public class HolderTest
{
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig().addPackageNames("com.jfireframework.context.test.function.loader");
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        Assert.assertEquals("name", person.getName());
        Home home = jfire.getBean(Home.class);
        Assert.assertEquals(100, home.getLength());
    }
}
