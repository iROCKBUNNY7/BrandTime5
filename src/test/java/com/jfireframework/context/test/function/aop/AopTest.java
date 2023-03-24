package com.jfireframework.context.test.function.aop;

import com.jfireframework.context.test.function.aop.Enhance.EnhanceForOrder;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.prepare.annotation.ComponentScan;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AopTest
{
    @Configuration
    @ComponentScan("com.jfireframework.context.test.function.aop")
    public static class AopTtestScan
    {

    }

    @Test
    public void beforetest()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap(AopTtestScan.class);
        Jfire          jfire       = jfireConfig.start();
        Person         person      = jfire.getBean(Person.class);
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
        JfireBootstrap jfireConfig = new JfireBootstrap(AopTtestScan.class);
        Jfire          jfire       = jfireConfig.start();
        Person         person      = jfire.getBean(Person.class);
        person.testInts(new int[]{1, 2, 3});
        assertEquals(1, person.invokeCount());

    }

    /**
     * 测试order的顺序问题。order数字大的先拦截。该方法被拦截两次，因此最终的order值应该是4
     */
    @Test
    public void testOrder()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap(AopTtestScan.class);
        Jfire          jfire       = jfireConfig.start();
        Person         person      = jfire.getBean(Person.class);
        person.order();
        assertEquals("EnhanceForOrder_enhance", EnhanceForOrder.result);
    }

    /**
     * 后置拦截可以拦截到方法调用后的结果值。
     */
    @Test
    public void testOrder2()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap(AopTtestScan.class);
        Jfire          jfire       = jfireConfig.start();
        Person         person      = jfire.getBean(Person.class);
        person.order2("林斌", 25);
        Enhance enhance = jfire.getBean(Enhance.class);
        assertEquals("林斌25", enhance.getResult());
    }

    @Test
    public void testMyname()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap(AopTtestScan.class);
        Jfire          jfire       = jfireConfig.start();
        Person         person      = jfire.getBean(Person.class);
        assertEquals("林斌你好", person.myName("你好"));
    }

    @Test
    public void testForVoidReturn()
    {
        JfireBootstrap bootstrap   = new JfireBootstrap(AopTtestScan.class);
        Jfire          jfire       = bootstrap.start();
        Person         person      = jfire.getBean(Person.class);
        assertEquals(0, person.invokeCount());
        person.testForVoidReturn();
        assertEquals(2, person.invokeCount());
    }

    @Test
    public void testThrow()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap(AopTtestScan.class);
        Jfire          jfire       = jfireConfig.start();
        Person         person      = jfire.getBean(Person.class);
        try
        {
            person.throwe();
        } catch (Exception e)
        {
            e.printStackTrace();
            assertEquals("aaaa", e.getMessage());
        }
    }

    @Test
    public void testTx()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap(AopTtestScan.class);
        Jfire          jfire       = jfireConfig.start();
        Person         person      = jfire.getBean(Person.class);
        person.tx();
        person.autoClose();
        TxManager txManager = jfire.getBean(TxManager.class);
        Assert.assertTrue(txManager.isBeginTransAction());
        Assert.assertTrue(txManager.isCommit());
    }
}
