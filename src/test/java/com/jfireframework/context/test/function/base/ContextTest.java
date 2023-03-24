package com.jfireframework.context.test.function.base;

import com.jfireframework.context.test.function.base.data.House;
import com.jfireframework.context.test.function.base.data.ImmutablePerson;
import com.jfireframework.context.test.function.base.data.MutablePerson;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.prepare.annotation.ComponentScan;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContextTest
{
    private static final Logger logger = LoggerFactory.getLogger(ContextTest.class);

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
        JfireBootstrap jfireConfig = new JfireBootstrap(ContextTestScan.class);
        baseTest(jfireConfig.start());
    }

    private void baseTest(Jfire jfire)
    {
        ImmutablePerson immutablePerson = jfire.getBean(ImmutablePerson.class);
        ImmutablePerson person2         = jfire.getBean(ImmutablePerson.class.getName());
        assertEquals(immutablePerson, person2);
        MutablePerson mutablePerson  = jfire.getBean(MutablePerson.class);
        MutablePerson mutablePerson2 = jfire.getBean(MutablePerson.class);
        assertNotEquals(mutablePerson, mutablePerson2);
        logger.debug(mutablePerson.getHome().getName());
        assertEquals(mutablePerson.getHome(), immutablePerson.getHome());
        assertEquals("林斌的房子", jfire.getBean(House.class).getName());
    }

    @Test
    public void testDirect()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap();
        jfireConfig.register(House.class);
        jfireConfig.register(MutablePerson.class);
        jfireConfig.register(ImmutablePerson.class);
        baseTest(jfireConfig.start());
    }

    @Test
    public void testDirect2()
    {
        JfireBootstrap jfireConfig    = new JfireBootstrap();
        BeanDefinition beanDefinition = new BeanDefinition(House.class.getName(), House.class, false);
        beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(House.class));
        jfireConfig.register(beanDefinition);
        jfireConfig.register(MutablePerson.class);
        jfireConfig.register(ImmutablePerson.class);
        baseTest(jfireConfig.start());
    }

    @Test
    public void testInit2()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap(ContextTestScan.class);
        Jfire          jfire       = jfireConfig.start();
        assertEquals("林斌的房子", jfire.getBean(House.class).getName());
        assertEquals("林斌的房子", ((House) jfire.getBean(House.class.getName())).getName());
    }
}
