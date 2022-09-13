package com.jfireframework.context.test.function.beanannotest;

import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.kernel.Jfire;

public class BeanAnnoTest
{
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig(Data.class);
        Jfire jfire = jfireConfig.build();
        Person person = jfire.getBean("person");
        Assert.assertTrue(person != null);
        Person person2 = jfire.getBean("person2");
        Assert.assertTrue(person2 == null);
        jfireConfig = new JfireConfig(Data.class);
        Properties properties = new Properties();
        properties.put("person2", "pass");
        jfireConfig.addProperties(properties);
        jfire = jfireConfig.build();
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
        Person person8 = jfire.getBean("person8");
        Assert.assertEquals("insertPerson8", person8.getName());
    }
    
}
