package com.jfirer.jfire.test.function.map;

import com.jfirer.jfire.core.AnnotatedApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapTest
{
    @Configuration
    @ComponentScan("com.jfirer.jfire.test.function.map")
    public static class MapTestScan
    {

    }

    @Test
    public void test()
    {
        ApplicationContext context = new AnnotatedApplicationContext(MapTestScan.class);
        assertEquals(context.getBean(Host.class).getMap().get(1).getClass(), Order1.class);
        assertEquals(2, context.getBean(Host.class).getMap().size());
        assertEquals(2, context.getBean(Host.class).getMap2().size());
        assertEquals(context.getBean(Host.class).getMap2().get(Order1.class.getName()).getClass(), Order1.class);
    }
}
