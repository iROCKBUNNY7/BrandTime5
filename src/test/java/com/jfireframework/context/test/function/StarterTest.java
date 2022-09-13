package com.jfireframework.context.test.function;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.kernel.Jfire;
import com.jfireframework.jfire.support.JfirePrepared.Configuration;
import com.jfireframework.jfire.support.JfirePrepared.EnableAutoConfiguration;

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
        Jfire jfire = jfireConfig.build();
        MyStarter myStarter = jfire.getBean(MyStarter.class);
        Assert.assertNotNull(myStarter);
    }
    
}
