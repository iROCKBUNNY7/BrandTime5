package com.jfireframework.context.test.function.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import javax.annotation.Resource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.context.test.function.base.data.House;
import com.jfireframework.context.test.function.base.data.ImmutablePerson;
import com.jfireframework.context.test.function.base.data.MutablePerson;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.JfireInitFinish;
import com.jfireframework.jfire.aware.provider.ComponentScan;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.config.annotation.Configuration;

public class ContextTest
{
    private static final Logger logger = LoggerFactory.getLogger(ContextTest.class);
    
    @Configuration
    @ComponentScan("com.jfireframework.context.test.function.base")
    public static class ContextTestScan
    {
        
    }
    
    /**
     * 测试构造方法,并且测试单例的正确性与否
     */
    @Test
    public void testConstruction()
    {
        JfireConfig jfireConfig = new JfireConfig(ContextTestScan.class);
        baseTest(new Jfire(jfireConfig), 4);
    }
    
    private void baseTest(Jfire jfire, int expected)
    {
        assertEquals(expected, jfire.getBeanDefinitionByAnnotation(Resource.class).length);
        ImmutablePerson immutablePerson = jfire.getBean(ImmutablePerson.class);
        ImmutablePerson person2 = (ImmutablePerson) jfire.getBean(ImmutablePerson.class.getName());
        assertEquals(immutablePerson, person2);
        MutablePerson mutablePerson = jfire.getBean(MutablePerson.class);
        MutablePerson mutablePerson2 = jfire.getBean(MutablePerson.class);
        assertNotEquals(mutablePerson, mutablePerson2);
        logger.debug(mutablePerson.getHome().getName());
        assertEquals(mutablePerson.getHome(), immutablePerson.getHome());
        assertEquals("林斌的房子", jfire.getBean(House.class).getName());
        assertEquals(1, jfire.getBeanDefinitionByInterface(JfireInitFinish.class).length);
    }
    
    /**
     * 测试手动加入beanconfig,对对象的参数属性进行设置
     */
    @Test
    public void testParam()
    {
        JfireConfig jfireConfig = new JfireConfig(ContextTestScan.class);
        BeanDefinition beanInfo = new BeanDefinition();
        beanInfo.setBeanName(ImmutablePerson.class.getName());
        beanInfo.putParam("name", "林斌");
        beanInfo.putParam("age", "25");
        beanInfo.putParam("boy", "true");
        beanInfo.putParam("arrays", "12,1212,1212121");
        jfireConfig.registerBeanDefinition(beanInfo);
        Jfire jfire = new Jfire(jfireConfig);
        testParam(jfire);
        assertEquals("林斌的房子", jfire.getBean(House.class).getName());
        ImmutablePerson person = jfire.getBean(ImmutablePerson.class);
        String[] arrays = person.getArrays();
        assertEquals("12", arrays[0]);
        assertEquals("1212", arrays[1]);
        assertEquals("1212121", arrays[2]);
    }
    
    private void testParam(Jfire jfire)
    {
        ImmutablePerson person = jfire.getBean(ImmutablePerson.class);
        assertEquals(person.getAge(), 25);
        assertEquals(person.getName(), "林斌");
        assertEquals(person.getBoy(), true);
    }
    
    @Test
    public void testDirect()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(House.class);
        jfireConfig.registerBeanDefinition(MutablePerson.class);
        jfireConfig.registerBeanDefinition(ImmutablePerson.class);
        baseTest(new Jfire(jfireConfig), 3);
    }
    
    @Test
    public void testDirect2()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(House.class.getName(), false, House.class);
        jfireConfig.registerBeanDefinition(MutablePerson.class);
        jfireConfig.registerBeanDefinition(ImmutablePerson.class);
        baseTest(new Jfire(jfireConfig), 3);
    }
    
    @Test
    public void testInit()
    {
        JfireConfig jfireConfig = new JfireConfig(ContextTestScan.class);
        assertEquals(1, new Jfire(jfireConfig).getBeanDefinitionByInterface(JfireInitFinish.class).length);
    }
    
    @Test
    public void testInit2()
    {
        JfireConfig jfireConfig = new JfireConfig(ContextTestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        BeanDefinition bean = jfire.getBeanDefinition(House.class);
        assertEquals("林斌的房子", ((House) bean.getInstance()).getName());
        bean = jfire.getBeanDefinition(House.class.getName());
        assertEquals("林斌的房子", ((House) bean.getInstance()).getName());
    }
    
}
