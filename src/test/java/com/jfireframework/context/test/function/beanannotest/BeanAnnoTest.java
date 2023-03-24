package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.jfire.core.JfireBootstrap;
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
        JfireBootstrap jfireConfig = new JfireBootstrap(Data.class);
        Jfire          jfire       = jfireConfig.start();
        Person         person      = jfire.getBean("person");
        Assert.assertTrue(person != null);
        Person person2;
        try
        {
            person2 = jfire.getBean("person2");
        } catch (Exception e)
        {
            assertTrue(e instanceof BeanDefinitionCanNotFindException);
        }
        jfireConfig = new JfireBootstrap(Data.class);
        Properties properties = new Properties();
        properties.put("person2", "pass");
        jfireConfig.addProperties(properties);
        jfire = jfireConfig.start();
        person2 = jfire.getBean("person2");
        Assert.assertTrue(person2 != null);
        Person person4 = jfire.getBean("person4");
        Assert.assertEquals("linbin", person4.getName());
        Person person5 = jfire.getBean("person5");
        Assert.assertEquals("2", person5.getName());
        Assert.assertEquals("2", jfire.getBean(NeedPerson5.class).getPerson().getName());
        Person person6 = jfire.getBean("person6");
        Assert.assertEquals("myimport", person6.getName());
        Person person7 = jfire.getBean("person7");
        Assert.assertEquals("house2", person7.getName());
        Person person7_2 = jfire.getBean("person7");
        Assert.assertEquals(person7, person7_2);
    }
}
