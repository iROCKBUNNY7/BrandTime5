package com.jfireframework.context.test.function.aop;

import static org.junit.Assert.assertEquals;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.importer.provide.scan.ComponentScan;

public class AopTest
{
    @ComponentScan("com.jfireframework.context.test.function.aop")
    public static class AopTtestScan
    {
        
    }
    
    @Test
    public void beforetest()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        person.sayHello("你好");
        Enhance enhance = jfire.getBean(Enhance.class);
        assertEquals("你好", enhance.getParam());
    }
    
    /**
     * 测试环绕拦截，拦截了原始的结果，并返回自定义的结果
     */
    @Test
    public void testAround()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        assertEquals(0, person.testInts(new int[] { 1, 2, 3 }).length);
    }
    
    /**
     * 测试order的顺序问题。order数字大的先拦截。该方法被拦截两次，因此最终的order值应该是4
     */
    @Test
    public void testOrder()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        System.out.println(person.getClass());
        person.order();
        Enhance enhance = jfire.getBean(Enhance.class);
        assertEquals(4, enhance.getOrder());
    }
    
    /**
     * 后置拦截可以拦截到方法调用后的结果值。
     */
    @Test
    public void testOrder2()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        person.order2("林斌", 25);
        Enhance enhance = jfire.getBean(Enhance.class);
        assertEquals("林斌25", enhance.getResult());
    }
    
    @Test
    public void testMyname()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        assertEquals("林斌你好", person.myName("你好"));
    }
    
    @Test
    public void testThrow()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        try
        {
            person.throwe();
        }
        catch (Exception e)
        {
            assertEquals("aaaa", e.getMessage());
        }
    }
    
    @Test
    public void testTx()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        person.tx();
        person.autoClose();
        TxManager txManager = jfire.getBean(TxManager.class);
        Assert.assertTrue(txManager.isBeginTransAction());
        Assert.assertTrue(txManager.isCommit());
        AcManager acManager = jfire.getBean(AcManager.class);
        Assert.assertTrue(acManager.isClose());
        Assert.assertTrue(acManager.isOpen());
    }
    
    @Test
    public void testChildTx()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        ChildPerson person = jfire.getBean(ChildPerson.class);
        person.my();
        TxManager txManager = jfire.getBean(TxManager.class);
        Assert.assertTrue(txManager.isBeginTransAction());
        Assert.assertTrue(txManager.isCommit());
    }
}
