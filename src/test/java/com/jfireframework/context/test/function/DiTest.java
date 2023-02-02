package com.jfireframework.context.test.function;

import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.inject.notated.CanBeNull;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

public class DiTest
{
    public interface forDi
    {

    }

    @Resource
    public static class ForDi1 implements forDi
    {

    }

    @Resource
    public static class ForDi2 implements forDi
    {

    }

    @Resource
    public static class Holder
    {
        @Resource
        private List<forDi> f = new LinkedList<DiTest.forDi>();
    }

    @Resource
    public static class Holder2
    {
        @Resource(name = "di")
        private forDi di;
    }

    @Resource
    public static class Holder3
    {
        @CanBeNull
        @Resource(name = "di")
        private forDi di;
    }

    /**
     * 测试List注入
     */
    @Test
    public void test()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap();
        jfireConfig.register(ForDi1.class);
        jfireConfig.register(ForDi2.class);
        jfireConfig.register(Holder.class);
        Jfire jfire = jfireConfig.start();
        Holder holder = jfire.getBean(Holder.class);
        Assert.assertEquals(2, holder.f.size());
    }

    /**
     * 接口带有名称的注入
     */
    @Test
    public void test_2()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap();
        BeanDefinition beanDefinition = new BeanDefinition("di", ForDi1.class, false);
        beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(ForDi1.class));
        jfireConfig.register(beanDefinition);
        jfireConfig.register(Holder2.class);
        Jfire jfire = jfireConfig.start();
        Holder2 holder = jfire.getBean(Holder2.class);
        Assert.assertNotNull(holder.di);
    }

    /**
     * 接口没有实现，但是允许为空
     */
    @Test
    public void test_3()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap();
        jfireConfig.register(Holder3.class);
        Jfire jfire = jfireConfig.start();
        Holder3 holder = jfire.getBean(Holder3.class);
        Assert.assertNull(holder.di);
    }
}
