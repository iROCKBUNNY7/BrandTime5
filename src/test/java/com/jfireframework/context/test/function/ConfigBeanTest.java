package com.jfireframework.context.test.function;

import com.jfireframework.jfire.core.AnnotatedApplicationContext;
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
        ApplicationContext context = new AnnotatedApplicationContext(ConfigBeanTest.class);
        Home               home    = context.getBean(Home.class);
        Person             person  = context.getBean(Person.class);
        Assert.assertNotNull(person);
        Assert.assertTrue(person == home.person);
    }
}
