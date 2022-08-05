package com.jfireframework.context.test.function.initmethod;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.importer.provide.ComponentScan;

public class InitMethodTest
{
    @Configuration
    @ComponentScan("com.jfireframework.context.test.function.initmethod")
    public static class InitMethodTestScan
    {
        
    }
    
    @Test
    public void test()
    {
        JfireConfig config = new JfireConfig(InitMethodTestScan.class);
        Person person = new Jfire(config).getBean(Person.class);
        Assert.assertEquals(23, person.getAge());
        Assert.assertEquals("林斌", person.getName());
    }
    
    @Test
    public void testcfg()
    {
        JfireConfig config = new JfireConfig(InitMethodTestScan.class);
        BeanDefinition beanInfo = new BeanDefinition();
        beanInfo.setBeanName("p2");
        beanInfo.setPostConstructMethod("initage");
        config.registerBeanDefinition(beanInfo);
        Jfire jfire = new Jfire(config);
        Person2 person2 = jfire.getBean(Person2.class);
        System.out.println("dsasdasd");
        Assert.assertEquals(12, person2.getAge());
    }
}
