package com.jfireframework.context.test.function.loader;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.config.JfireInitializationCfg;

public class HolderTest
{
    @Test
    public void test()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.loader");
        JfireConfig jfireConfig = new JfireConfig(cfg);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        Assert.assertEquals("name", person.getName());
        Home home = jfire.getBean(Home.class);
        Assert.assertEquals(100, home.getLength());
    }
}
