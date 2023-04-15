package com.jfirer.jfire.test.function.aop;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.test.function.aop.Enhance.EnhanceForOrder;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AopTest
{
    @Configuration
    @ComponentScan("com.jfirer.jfire.test.function.aop")
    public static class AopTtestScan
    {

    }

    @Test
    public void beforetest()
    {
        ApplicationContext context = new DefaultApplicationContext(AopTtestScan.class);
        Person             person  = context.getBean(Person.class);
        person.sayHello("你好");
        Enhance enhance = context.getBean(Enhance.class);
        assertEquals("你好", enhance.getParam());
    }

    /**
     * 测试环绕拦截，拦截了原始的结果，并返回自定义的结果
     */
    @Test
    public void testAround()
    {
        ApplicationContext context = new DefaultApplicationContext(AopTtestScan.class);
        Person             person  = context.getBean(Person.class);
        person.testInts(new int[]{1, 2, 3});
        assertEquals(1, person.invokeCount());
    }

    /**
     * 测试order的顺序问题。order数字大的先拦截。该方法被拦截两次，因此最终的order值应该是4
     */
    @Test
    public void testOrder()
    {
        ApplicationContext context = new DefaultApplicationContext(AopTtestScan.class);
        Person             person  = context.getBean(Person.class);
        person.order();
        assertEquals("EnhanceForOrder_enhance", EnhanceForOrder.result);
    }

    /**
     * 后置拦截可以拦截到方法调用后的结果值。
     */
    @Test
    public void testOrder2()
    {
        ApplicationContext context = new DefaultApplicationContext(AopTtestScan.class);
        Person             person  = context.getBean(Person.class);
        person.order2("林斌", 25);
        Enhance enhance = context.getBean(Enhance.class);
        assertEquals("林斌25", enhance.getResult());
    }

    @Test
    public void testMyname()
    {
        ApplicationContext context = new DefaultApplicationContext(AopTtestScan.class);
        Person             person  = context.getBean(Person.class);
        assertEquals("林斌你好", person.myName("你好"));
    }

    @Test
    public void testForVoidReturn()
    {
        ApplicationContext context = new DefaultApplicationContext(AopTtestScan.class);
        Person             person  = context.getBean(Person.class);
        assertEquals(0, person.invokeCount());
        person.testForVoidReturn();
        assertEquals(2, person.invokeCount());
    }

    @Test
    public void testThrow()
    {
        ApplicationContext context = new DefaultApplicationContext(AopTtestScan.class);
        Person             person  = context.getBean(Person.class);
        try
        {
            person.throwe();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            assertEquals("aaaa", e.getMessage());
        }
    }

    @Test
    public void testTx()
    {
        ApplicationContext context = new DefaultApplicationContext(AopTtestScan.class);
        Person             person  = context.getBean(Person.class);
        person.tx();
        person.autoClose();
        TxManager txManager = context.getBean(TxManager.class);
        Assert.assertTrue(txManager.isBeginTransAction());
        Assert.assertTrue(txManager.isCommit());
    }

    @Test
    public void testAopAndFiled()
    {
        ApplicationContext context = new DefaultApplicationContext(AopTtestScan.class);
        context.register(Home.class);
        Person person = context.getBean(Person.class);
        assertNotNull(person.getHome());
    }
}
