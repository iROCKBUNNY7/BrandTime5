package com.jfireframework.context.test.function.cachetest;

import javax.annotation.Resource;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.Cache;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.CacheManager;

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
