package com.jfireframework.context.test.function.cachetest;

import javax.annotation.Resource;
import com.jfireframework.jfire.core.aop.impl.CacheAopManager.Cache;
import com.jfireframework.jfire.core.aop.impl.CacheAopManager.CacheManager;

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
