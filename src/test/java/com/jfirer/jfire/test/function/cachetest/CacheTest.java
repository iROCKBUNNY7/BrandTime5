package com.jfirer.jfire.test.function.cachetest;

import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.test.function.base.data.House;
import com.jfirer.jfire.test.function.base.data.MutablePerson;
import org.junit.Assert;
import org.junit.Test;

public class CacheTest
{
    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        context.refresh();
        CacheTarget cacheTarget = context.getBean(CacheTarget.class);
        House       house       = cacheTarget.get(1);
        House       second      = cacheTarget.get(1);
        Assert.assertFalse(house == second);
        house = cacheTarget.get(5);
        second = cacheTarget.get(5);
        Assert.assertTrue(house == second);
        cacheTarget.put(5);
        second = cacheTarget.get(5);
        Assert.assertFalse(house == second);
        house = cacheTarget.get(5);
        second = cacheTarget.get(5);
        Assert.assertTrue(house == second);
        cacheTarget.remove(5);
        second = cacheTarget.get(5);
        Assert.assertFalse(house == second);
        String first   = cacheTarget.get();
        String seconde = cacheTarget.get();
        Assert.assertTrue(first == seconde);
        cacheTarget.put();
        seconde = cacheTarget.get();
        Assert.assertFalse(first == seconde);
        first = cacheTarget.get();
        Assert.assertTrue(first == seconde);
    }

    /**
     * 测试覆盖补全
     */
    @Test
    public void test_2()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        context.refresh();
        CacheTarget cacheTarget = context.getBean(CacheTarget.class);
        House       house       = cacheTarget.get2(6);
        House       second      = cacheTarget.get2(6);
        Assert.assertTrue(house == second);
    }

    /**
     * 测试覆盖补全
     */
    @Test
    public void test_3()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        context.refresh();
        CacheTarget cacheTarget = context.getBean(CacheTarget.class);
        House       house       = cacheTarget.get3(6);
        House       second      = cacheTarget.get3(6);
        Assert.assertTrue(house == second);
    }

    /**
     * 测试覆盖补全
     */
    @Test
    public void test_4()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        context.refresh();
        CacheTarget   cacheTarget = context.getBean(CacheTarget.class);
        MutablePerson person      = new MutablePerson();
        person.setAge(18);
        House house  = cacheTarget.get4(person);
        House second = cacheTarget.get4(person);
        Assert.assertTrue(house == second);
    }
}
