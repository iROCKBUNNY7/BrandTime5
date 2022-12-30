package com.jfireframework.context.test.function.map;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.prepare.impl.ComponentScan;
import com.jfireframework.jfire.core.prepare.impl.Configuration;

public class MapTest
{
    @Configuration
    @ComponentScan("com.jfireframework.context.test.function.map")
    public static class MapTestScan
    {
        
    }
    
    @Test
    public void test()
    {
        JfireBootstrap config = new JfireBootstrap(MapTestScan.class);
        Jfire jfire = config.start();
        assertEquals(jfire.getBean(Host.class).getMap().get(1).getClass(), Order1.class);
        assertEquals(2, jfire.getBean(Host.class).getMap().size());
        assertEquals(2, jfire.getBean(Host.class).getMap2().size());
        assertEquals(jfire.getBean(Host.class).getMap2().get(Order1.class.getName()).getClass(), Order1.class);
    }
    
}
