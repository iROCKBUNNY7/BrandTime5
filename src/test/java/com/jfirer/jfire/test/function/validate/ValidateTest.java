package com.jfirer.jfire.test.function.validate;

import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.aop.support.validate.JfireMethodValidatorImpl;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ValidationException;

public class ValidateTest
{

    @Test
    public void test_2()
    {
        ApplicationContext context = new DefaultApplicationContext();
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
            Assert.assertEquals("sayHello.user.name,", e.getMessage());
        }
        try
        {
            person.sayHello2(null);
            Assert.fail();
        }
        catch (ValidationException e)
        {
            Assert.assertEquals("sayHello2.name,", e.getMessage());
        }
    }
}
