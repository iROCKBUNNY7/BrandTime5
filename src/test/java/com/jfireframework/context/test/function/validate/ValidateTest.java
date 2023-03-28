package com.jfireframework.context.test.function.validate;

import com.jfireframework.jfire.core.AnnotatedApplicationContext;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.aop.support.validate.JfireMethodValidatorImpl;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ValidationException;

public class ValidateTest
{

    @Test
    public void test_2()
    {
        ApplicationContext context = new AnnotatedApplicationContext();
        context.register(JfireMethodValidatorImpl.class);
        context.register(Person.class);
        Person person = context.getBean(Person.class);
        User   user   = new User();
        try
        {
            person.sayHello(user);
            Assert.fail();
        }
        catch (ValidationException e)
        {
            Assert.assertEquals("sayHello.0.name,", e.getMessage());
        }
        try
        {
            person.sayHello2(null);
            Assert.fail();
        }
        catch (ValidationException e)
        {
            Assert.assertEquals("sayHello2.0,", e.getMessage());
        }
    }
}
