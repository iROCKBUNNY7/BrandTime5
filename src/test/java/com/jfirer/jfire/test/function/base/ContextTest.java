package com.jfirer.jfire.test.function.base;

import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.test.function.base.data.House;
import com.jfirer.jfire.test.function.base.data.ImmutablePerson;
import com.jfirer.jfire.test.function.base.data.MutablePerson;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContextTest
{
    private static final Logger logger = LoggerFactory.getLogger(ContextTest.class);

    @ComponentScan("com.jfirer.jfire.test.function.base")
    public static class ContextTestScan
    {

    }

    /**
     * 测试构造方法,并且测试单例的正确性与否
     */
    @Test
    public void testConstruction()
    {
        ApplicationContext context = new DefaultApplicationContext(ContextTestScan.class);
        baseTest(context);
    }

    private void baseTest(ApplicationContext context)
    {
        ImmutablePerson immutablePerson = context.getBean(ImmutablePerson.class);
        ImmutablePerson person2         = context.getBean(ImmutablePerson.class.getName());
        assertEquals(immutablePerson, person2);
        MutablePerson mutablePerson  = context.getBean(MutablePerson.class);
        MutablePerson mutablePerson2 = context.getBean(MutablePerson.class);
        assertNotEquals(mutablePerson, mutablePerson2);
        logger.debug(mutablePerson.getHome().getName());
        assertEquals(mutablePerson.getHome(), immutablePerson.getHome());
        assertEquals("林斌的房子", context.getBean(House.class).getName());
    }

    @Test
    public void testDirect()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(House.class);
        context.register(MutablePerson.class);
        context.register(ImmutablePerson.class);
        context.refresh();
        baseTest(context);
    }


    @Test
    public void testInit2()
    {
        ApplicationContext context = new DefaultApplicationContext(ContextTestScan.class);
        assertEquals("林斌的房子", context.getBean(House.class).getName());
        assertEquals("林斌的房子", ((House) context.getBean(House.class.getName())).getName());
    }
}
