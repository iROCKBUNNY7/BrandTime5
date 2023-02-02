package com.jfireframework.context.test.function;

import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.prepare.impl.Configuration;
import com.jfireframework.jfire.core.prepare.impl.EnableAutoConfiguration;
import org.junit.Assert;
import org.junit.Test;

@EnableAutoConfiguration
@Configuration
public class StarterTest
{
    public static class MyStarter
    {
    }

    @Test
    public void test()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap(StarterTest.class);
        Jfire jfire = jfireConfig.start();
        MyStarter myStarter = jfire.getBean(MyStarter.class);
        Assert.assertNotNull(myStarter);
    }

}
