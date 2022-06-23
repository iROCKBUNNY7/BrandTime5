package com.jfireframework.context.test.function.aop;

import static org.junit.Assert.assertEquals;
import java.lang.reflect.Method;
import javax.annotation.Resource;
import org.junit.Test;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.config.JfireInitializationCfg;
import com.jfireframework.jfire.util.EnvironmentUtil;

public class AopTest
{
    @Test
    public void testAnnoExist() throws NoSuchMethodException, SecurityException
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.aop");
        JfireConfig jfireConfig = new JfireConfig(cfg);
        Jfire jfire = new Jfire(jfireConfig);
        BeanDefinition bean = jfire.getBeanDefinition(Person.class);
        Method method = bean.getType().getDeclaredMethod("sayHello");
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        assertEquals("注解保留", annotationUtil.getAnnotation(Resource.class, method).name());
    }
    
    @Test
    public void beforetest()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.aop");
        JfireConfig jfireConfig = new JfireConfig(cfg);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        assertEquals("前置拦截", person.sayHello("你好"));
    }
    
    @Test
    public void beforeTest2()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.aop");
        JfireConfig jfireConfig = new JfireConfig(cfg);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        assertEquals(0, person.testInts(new int[] { 1, 2, 3 }).length);
    }
    
    @Test
    public void testOrder()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.aop");
        JfireConfig jfireConfig = new JfireConfig(cfg);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        System.out.println(person.getClass());
        assertEquals("3", person.order());
    }
    
    @Test
    public void testOrder2()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.aop");
        JfireConfig jfireConfig = new JfireConfig(cfg);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        assertEquals("你好", person.order2("林斌", 25));
    }
    
    @Test
    public void testMyname()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.aop");
        JfireConfig jfireConfig = new JfireConfig(cfg);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        assertEquals("林斌你好", person.myName("你好"));
    }
    
    @Test
    public void testThrow()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.aop");
        JfireConfig jfireConfig = new JfireConfig(cfg);
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
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.aop");
        JfireConfig jfireConfig = new JfireConfig(cfg);
        Jfire jfire = new Jfire(jfireConfig);
        Person person = jfire.getBean(Person.class);
        person.tx();
        person.autoClose();
    }
    
    @Test
    public void testChildTx()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.aop");
        JfireConfig jfireConfig = new JfireConfig(cfg);
        Jfire jfire = new Jfire(jfireConfig);
        ChildPerson person = jfire.getBean(ChildPerson.class);
        person.my();
    }
}
