package com.jfireframework.context.test.function;

import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Bean;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

@Configuration
@Resource
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
        JfireBootstrap jfireConfig = new JfireBootstrap();
        jfireConfig.register(ConfigBeanTest.class);
        Jfire jfire = jfireConfig.start();
        Home home = jfire.getBean(Home.class);
        Person person = jfire.getBean(Person.class);
        Assert.assertNotNull(person);
        Assert.assertTrue(person == home.person);
    }
}
