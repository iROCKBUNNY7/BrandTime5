package com.jfireframework.context.test.function.lazyinit;

import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.importer.provide.ComponentScan;

@Resource
public class LazyInitTest
{
    @Resource
    private OriginInstance  instance;
    @Resource
    private OriginInstance2 instance2;
    @Resource
    private OriginInstance3 instance3;
    public static int       invokedCount  = 0;
    public static int       invokedCount2 = 0;
    public static int       invokedCount3 = 0;
    
    public String name()
    {
        return instance.getName();
    }
    
    public String name2()
    {
        return instance2.name();
    }
    
    public String name3()
    {
        return instance3.name();
    }
    
    @Configuration
    @ComponentScan("com.jfireframework.context.test.function.lazyinit")
    public static class scan
    {
        
    }
    
    @Before
    public void before()
    {
        invokedCount = 0;
        invokedCount2 = 0;
        invokedCount3 = 0;
    }
    
    /**
     * 测试@LazyInitUniltFirstInvoke打在类上。此时是单例模式，因此多次调用不会增加构造方法被多次调用
     */
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig(scan.class);
        jfireConfig.registerBeanDefinition(OriginInstance.class.getName(), false, OriginInstance.class);
        Jfire jfire = new Jfire(jfireConfig);
        LazyInitTest container = jfire.getBean(LazyInitTest.class);
        Assert.assertEquals(0, invokedCount);
        container.name();
        Assert.assertEquals(1, invokedCount);
        container.name();
        Assert.assertEquals(1, invokedCount);
    }
    
    /**
     * 测试@LazyInitUniltFirstInvoke打在类上。此时是原型模式，因此多次调用会增加构造方法被多次调用
     */
    @Test
    public void test_2()
    {
        JfireConfig jfireConfig = new JfireConfig(scan.class);
        jfireConfig.registerBeanDefinition(OriginInstance.class.getName(), true, OriginInstance.class);
        Jfire jfire = new Jfire(jfireConfig);
        LazyInitTest container = jfire.getBean(LazyInitTest.class);
        Assert.assertEquals(0, invokedCount);
        container.name();
        Assert.assertEquals(1, invokedCount);
        container.name();
        Assert.assertEquals(2, invokedCount);
    }
    
    /**
     * 测试注解@LazyInitUniltFirstInvoke打在方法上。此时是单例模式，因此多次调用不会增加构造方法被多次调用
     */
    @Test
    public void test_3()
    {
        JfireConfig jfireConfig = new JfireConfig(scan.class);
        jfireConfig.registerBeanDefinition(OriginInstance.class.getName(), false, OriginInstance.class);
        Jfire jfire = new Jfire(jfireConfig);
        LazyInitTest container = jfire.getBean(LazyInitTest.class);
        Assert.assertEquals(0, invokedCount2);
        container.name2();
        Assert.assertEquals(1, invokedCount2);
        container.name2();
        Assert.assertEquals(1, invokedCount2);
    }
    
    /**
     * 测试注解@LazyInitUniltFirstInvoke打在方法上。此时是原型模式，因此多次调用会增加构造方法被多次调用
     */
    @Test
    public void test_4()
    {
        JfireConfig jfireConfig = new JfireConfig(scan.class);
        jfireConfig.registerBeanDefinition(OriginInstance.class.getName(), false, OriginInstance.class);
        Jfire jfire = new Jfire(jfireConfig);
        LazyInitTest container = jfire.getBean(LazyInitTest.class);
        Assert.assertEquals(0, invokedCount3);
        container.name3();
        Assert.assertEquals(1, invokedCount3);
        container.name3();
        Assert.assertEquals(2, invokedCount3);
    }
}
