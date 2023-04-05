package com.jfirer.jfire.test.function.cachetest;

import com.jfirer.jfire.core.AnnotatedApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.test.function.base.data.House;
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
