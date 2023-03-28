package com.jfireframework.context.test.function.cachetest;

import com.jfireframework.context.test.function.base.data.House;
import com.jfireframework.jfire.core.AnnotatedApplicationContext;
import com.jfireframework.jfire.core.ApplicationContext;
import org.junit.Test;

public class NewCacheTest
{
    @Test
    public void test()
    {
        ApplicationContext context = new AnnotatedApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        context.refresh();
        CacheTarget cacheTarget = context.getBean(CacheTarget.class);
        House       house       = cacheTarget.get(5);
        System.out.println(house);
        house = cacheTarget.get(5);
        System.out.println(house);
    }
}
