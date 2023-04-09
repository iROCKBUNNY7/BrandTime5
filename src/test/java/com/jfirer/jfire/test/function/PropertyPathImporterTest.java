package com.jfirer.jfire.test.function;

import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.inject.notated.PropertyRead;
import com.jfirer.jfire.core.prepare.annotation.PropertyPath;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

@Resource
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
        ApplicationContext context = new DefaultApplicationContext(Test1.class);
        context.register(PropertyPathImporterTest.class);
        PropertyPathImporterTest test = context.getBean(PropertyPathImporterTest.class);
        Assert.assertEquals(12, test.age);
    }

    /**
     * 使用文件路径读取
     */
    @Test
    public void test2()
    {
        ApplicationContext context = new DefaultApplicationContext(Test2.class);
        context.register(PropertyPathImporterTest.class);
        PropertyPathImporterTest test = context.getBean(PropertyPathImporterTest.class);
        Assert.assertEquals(12, test.age);
    }
}
