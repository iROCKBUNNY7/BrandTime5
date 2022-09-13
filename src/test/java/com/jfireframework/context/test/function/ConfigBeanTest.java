package com.jfireframework.context.test.function;

import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.kernel.Jfire;
import com.jfireframework.jfire.support.JfirePrepared.Configuration;
import com.jfireframework.jfire.support.JfirePrepared.Configuration.Bean;

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
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(ConfigBeanTest.class);
        Jfire jfire = jfireConfig.build();
        Home home = jfire.getBean(Home.class);
        Person person = jfire.getBean(Person.class);
        Assert.assertNotNull(person);
        Assert.assertTrue(person == home.person);
    }
}
