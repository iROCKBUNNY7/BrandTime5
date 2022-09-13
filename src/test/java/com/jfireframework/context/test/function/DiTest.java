package com.jfireframework.context.test.function;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.kernel.Jfire;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.field.CanBeNull;

public class DiTest
{
    public static interface forDi
    {
        
    }
    
    public static class ForDi1 implements forDi
    {
        
    }
    
    public static class ForDi2 implements forDi
    {
        
    }
    
    public static class Holder
    {
        @Resource
        private List<forDi> f = new LinkedList<DiTest.forDi>();
    }
    
    public static class Holder2
    {
        @Resource(name = "di")
        private forDi di;
    }
    
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
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(ForDi1.class, ForDi2.class);
        jfireConfig.registerBeanDefinition(Holder.class);
        Jfire jfire = jfireConfig.build();
        Holder holder = jfire.getBean(Holder.class);
        Assert.assertEquals(2, holder.f.size());
    }
    
    /**
     * 接口带有名称的注入
     */
    @Test
    public void test_2()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition("di", false, ForDi1.class);
        jfireConfig.registerBeanDefinition(Holder2.class);
        Jfire jfire = jfireConfig.build();
        Holder2 holder = jfire.getBean(Holder2.class);
        Assert.assertNotNull(holder.di);
    }
    
    /**
     * 接口没有实现，但是允许为空
     */
    @Test
    public void test_3()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(Holder3.class);
        Jfire jfire = jfireConfig.build();
        Holder3 holder = jfire.getBean(Holder3.class);
        Assert.assertNull(holder.di);
    }
}
