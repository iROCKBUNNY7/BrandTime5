package com.jfireframework.context.test.function.validate;

import javax.validation.ValidationException;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;

public class ValidateTest
{
    
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
            Assert.fail();
        }
        catch (ValidationException e)
        {
            Assert.assertEquals("{com.jfireframework.context.test.function.validate.Person.sayHello_0.name : may not be null}", e.getMessage());
        }
        try
        {
            person.sayHello2(null);
            Assert.fail();
        }
        catch (ValidationException e)
        {
            Assert.assertEquals("{com.jfireframework.context.test.function.validate.Person.sayHello2_0 : may not be null}", e.getMessage());
        }
    }
}
