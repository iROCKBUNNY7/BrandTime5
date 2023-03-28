package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.jfire.core.AnnotatedApplicationContext;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.exception.BeanDefinitionCanNotFindException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class BeanAnnoTest
{
    @Test
    public void test()
    {
        ApplicationContext context = new AnnotatedApplicationContext(Data.class);
        Person             person  = context.getBean("person");
        Assert.assertTrue(person != null);
        Person person2;
        try
        {
            person2 = context.getBean("person2");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof BeanDefinitionCanNotFindException);
        }
        context = new AnnotatedApplicationContext(Data.class);
        Properties properties = new Properties();
        properties.put("person2", "pass");
        context.getEnv().addProperties(properties);
        person2 = context.getBean("person2");
        Assert.assertTrue(person2 != null);
        Person person4 = context.getBean("person4");
        Assert.assertEquals("linbin", person4.getName());
        Person person5 = context.getBean("person5");
        Assert.assertEquals("2", person5.getName());
        Assert.assertEquals("2", context.getBean(NeedPerson5.class).getPerson().getName());
        Person person6 = context.getBean("person6");
        Assert.assertEquals("myimport", person6.getName());
        Person person7 = context.getBean("person7");
        Assert.assertEquals("house2", person7.getName());
        Person person7_2 = context.getBean("person7");
        Assert.assertEquals(person7, person7_2);
    }
}
