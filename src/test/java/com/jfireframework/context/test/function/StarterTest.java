package com.jfireframework.context.test.function;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.aware.provider.EnableAutoConfiguration;
import com.jfireframework.jfire.config.annotation.Configuration;

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
        JfireConfig jfireConfig = new JfireConfig(StarterTest.class);
        Jfire jfire = new Jfire(jfireConfig);
        MyStarter myStarter = jfire.getBean(MyStarter.class);
        Assert.assertNotNull(myStarter);
    }
    
}
