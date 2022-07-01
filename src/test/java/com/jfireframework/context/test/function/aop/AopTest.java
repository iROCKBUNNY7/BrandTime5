package com.jfireframework.context.test.function.aop;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.inittrigger.provide.scan.ComponentScan;

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
        assertEquals("前置拦截", person.sayHello("你好"));
    }
    
    @Test
    public void beforeTest2()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        assertEquals(0, person.testInts(new int[] { 1, 2, 3 }).length);
    }
    
    @Test
    public void testOrder()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        System.out.println(person.getClass());
        assertEquals("3", person.order());
    }
    
    @Test
    public void testOrder2()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        assertEquals("你好", person.order2("林斌", 25));
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
    }
    
    @Test
    public void testChildTx()
    {
        JfireConfig jfireConfig = new JfireConfig(AopTtestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        ChildPerson person = jfire.getBean(ChildPerson.class);
        person.my();
    }
}
