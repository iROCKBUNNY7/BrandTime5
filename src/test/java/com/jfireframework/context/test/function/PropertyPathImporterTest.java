package com.jfireframework.context.test.function;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.bean.annotation.field.PropertyRead;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.importer.provide.property.PropertyPath;

public class PropertyPathImporterTest
{
    @PropertyRead
    private int age;
    
    @Configuration
    @PropertyPath("classpath:propertiestest.properties")
    public static class Test1
    {
        
    }
    
    @Configuration
    @PropertyPath("file:src/test/resources/propertiestest.properties")
    public static class Test2
    {
        
    }
    
    /**
     * 使用classpath路径读取
     */
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig(Test1.class);
        jfireConfig.registerBeanDefinition(PropertyPathImporterTest.class);
        Jfire jfire = new Jfire(jfireConfig);
        PropertyPathImporterTest test = jfire.getBean(PropertyPathImporterTest.class);
        Assert.assertEquals(12, test.age);
    }
    
    /**
     * 使用文件路径读取
     */
    @Test
    public void test2()
    {
        JfireConfig jfireConfig = new JfireConfig(Test2.class);
        jfireConfig.registerBeanDefinition(PropertyPathImporterTest.class);
        Jfire jfire = new Jfire(jfireConfig);
        PropertyPathImporterTest test = jfire.getBean(PropertyPathImporterTest.class);
        Assert.assertEquals(12, test.age);
    }
}
