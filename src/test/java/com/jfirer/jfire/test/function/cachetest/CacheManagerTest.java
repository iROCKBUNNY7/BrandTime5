package com.jfirer.jfire.test.function.cachetest;

import com.jfirer.jfire.core.aop.impl.CacheAopManager.Cache;
import com.jfirer.jfire.core.aop.impl.CacheAopManager.CacheManager;

import javax.annotation.Resource;

@Resource
public class CacheManagerTest implements CacheManager
{

    private Cache cahce = new DemoCache();

    @Override
    public Cache get(String name)
    {
        System.out.println("cache名称：" + name);
        return cahce;
    }
}
