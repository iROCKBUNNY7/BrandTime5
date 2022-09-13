package com.jfireframework.context.test.function.cachetest;

import org.junit.Test;
import com.jfireframework.context.test.function.base.data.House;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.kernel.Jfire;

public class NewCacheTest
{
    @Test
    public void test()
    {
        JfireConfig config = new JfireConfig();
        config.registerBeanDefinition(CacheTarget.class);
        config.registerBeanDefinition(DemoCache.class);
        config.registerBeanDefinition(CacheManagerTest.class);
        Jfire jfire = config.build();
        CacheTarget cacheTarget = jfire.getBean(CacheTarget.class);
        House house = cacheTarget.get(5);
        System.out.println(house);
        house = cacheTarget.get(5);
        System.out.println(house);
        
    }
}
