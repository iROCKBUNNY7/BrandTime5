package com.jfireframework.context.test.function.map;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.config.JfireInitializationCfg;

public class MapTest
{
    @Test
    public void test()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.map");
        JfireConfig config = new JfireConfig(cfg);
        Jfire jfire = new Jfire(config);
        assertEquals(jfire.getBean(Host.class).getMap().get(1).getClass(), Order1.class);
        assertEquals(2, jfire.getBean(Host.class).getMap().size());
        assertEquals(2, jfire.getBean(Host.class).getMap2().size());
        assertEquals(jfire.getBean(Host.class).getMap2().get(Order1.class.getName()).getClass(), Order1.class);
    }
    
}
