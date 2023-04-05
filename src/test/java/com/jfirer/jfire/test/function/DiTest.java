package com.jfirer.jfire.test.function;

import com.jfirer.jfire.core.AnnotatedApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.inject.notated.CanBeNull;
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

    @Resource(name = "di")
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
        ApplicationContext context = new AnnotatedApplicationContext();
        context.register(ForDi1.class);
        context.register(ForDi2.class);
        context.register(Holder.class);
        Holder holder = context.getBean(Holder.class);
        Assert.assertEquals(2, holder.f.size());
    }

    /**
     * 接口带有名称的注入
     */
    @Test
    public void test_2()
    {
        ApplicationContext context = new AnnotatedApplicationContext();
        context.register(ForDi1.class);
        context.register(Holder2.class);
        Holder2 holder = context.getBean(Holder2.class);
        Assert.assertNotNull(holder.di);
    }

    /**
     * 接口没有实现，但是允许为空
     */
    @Test
    public void test_3()
    {
        ApplicationContext context = new AnnotatedApplicationContext();
        context.register(Holder3.class);
        Holder3 holder = context.getBean(Holder3.class);
        Assert.assertNull(holder.di);
    }
}
