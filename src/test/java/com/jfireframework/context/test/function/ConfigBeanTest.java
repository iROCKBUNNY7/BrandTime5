package com.jfireframework.context.test.function;

import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Bean;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

@Configuration
public class ConfigBeanTest
{
    @Bean
    public Person person()
    {
        return new Person();
    }

    @Bean
    public Home home(Person person)
    {
        Home home = new Home();
        home.person = person;
        return home;
    }

    public static class Home
    {
        private Person person;
    }

    public static class Person
    {
    }

    @Test
    public void test()
    {
        JfireBootstrap     jfireConfig = new JfireBootstrap(ConfigBeanTest.class);
        ApplicationContext jfire       = jfireConfig.start();
        Home               home        = jfire.getBean(Home.class);
        Person             person      = jfire.getBean(Person.class);
        Assert.assertNotNull(person);
        Assert.assertTrue(person == home.person);
    }
}
