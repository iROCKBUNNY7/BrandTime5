package com.jfirer.jfire.test.function;

import com.jfirer.jfire.core.AnnotatedApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.EnableAutoConfiguration;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

@EnableAutoConfiguration
@Configuration
public class StarterTest
{
    @Configuration
    public static class MyStarter
    {

    }

    @Test
    public void test()
    {
        ApplicationContext context = new AnnotatedApplicationContext(StarterTest.class);
        MyStarter          myStarter   = context.getBean(MyStarter.class);
        Assert.assertNotNull(myStarter);
    }
}
