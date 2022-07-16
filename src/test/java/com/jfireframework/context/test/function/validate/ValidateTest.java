package com.jfireframework.context.test.function.validate;

import javax.validation.ValidationException;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;

public class ValidateTest
{
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(Person.class);
        jfireConfig.registerBeanDefinition(JfireMethodValidatorImpl.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        User user = new User();
        try
        {
            person.sayHello(user);
        }
        catch (ValidationException e)
        {
            Assert.assertEquals("name:测试", e.getMessage());
        }
    }
    
    @Test
    public void test_1()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(Person.class);
        jfireConfig.registerBeanDefinition(JfireMethodValidatorImpl.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        User user = new User();
        try
        {
            person.sayHello2(user);
        }
        catch (ValidationException e)
        {
            Assert.assertEquals("name:测试", e.getMessage());
        }
    }
    
    @Test
    public void test_2()
    {
        JfireConfig config = new JfireConfig();
        config.registerBeanDefinition(Person.class, com.jfireframework.jfire.validate.internal.JfireMethodValidatorImpl.class);
        Jfire jfire = new Jfire(config);
        Person person = jfire.getBean(Person.class);
        User user = new User();
        try
        {
            person.sayHello(user);
        }
        catch (ValidationException e)
        {
            Assert.assertEquals("name:may not be null", e.getMessage());
        }
    }
}
