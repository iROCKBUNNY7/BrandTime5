package com.jfireframework.context.test.function.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.context.test.function.base.data.House;
import com.jfireframework.context.test.function.base.data.ImmutablePerson;
import com.jfireframework.context.test.function.base.data.MutablePerson;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.JfireAwareContextInited;
import com.jfireframework.jfire.support.jfireprepared.ComponentScan;
import com.jfireframework.jfire.support.jfireprepared.Configuration;

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
        assertEquals(1, jfire.getBeanDefinitionByInterface(JfireAwareContextInited.class).length);
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
        assertEquals(1, new Jfire(jfireConfig).getBeanDefinitionByInterface(JfireAwareContextInited.class).length);
    }
    
    @Test
    public void testInit2()
    {
        JfireConfig jfireConfig = new JfireConfig(ContextTestScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        BeanDefinition bean = jfire.getBeanDefinition(House.class);
        Map<String, Object> map = new HashMap<String, Object>();
        assertEquals("林斌的房子", ((House) bean.getBeanInstanceResolver().getInstance(map)).getName());
        bean = jfire.getBeanDefinition(House.class.getName());
        map.clear();
        assertEquals("林斌的房子", ((House) bean.getBeanInstanceResolver().getInstance(map)).getName());
    }
    
}
