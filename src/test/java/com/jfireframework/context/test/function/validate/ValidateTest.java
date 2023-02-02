package com.jfireframework.context.test.function.validate;

import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.aop.support.validate.JfireMethodValidatorImpl;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ValidationException;

public class ValidateTest
{

    @Test
    public void test_2()
    {
        JfireBootstrap config = new JfireBootstrap();
        BeanDefinition beanDefinition = new BeanDefinition(JfireMethodValidatorImpl.class.getName(), JfireMethodValidatorImpl.class, false);
        BeanInstanceResolver resolver = new DefaultBeanInstanceResolver(JfireMethodValidatorImpl.class);
        beanDefinition.setBeanInstanceResolver(resolver);
        config.register(beanDefinition);
        config.register(Person.class);
        Jfire jfire = config.start();
        Person person = jfire.getBean(Person.class);
        User user = new User();
        try
        {
            person.sayHello(user);
            Assert.fail();
        } catch (ValidationException e)
        {
            Assert.assertEquals("sayHello.0.name,", e.getMessage());
        }
        try
        {
            person.sayHello2(null);
            Assert.fail();
        } catch (ValidationException e)
        {
            Assert.assertEquals("sayHello2.0,", e.getMessage());
        }
    }
}
