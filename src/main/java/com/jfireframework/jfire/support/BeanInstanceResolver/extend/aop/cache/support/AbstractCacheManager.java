package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.support;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.Cache;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.CacheManager;

public abstract class AbstractCacheManager implements CacheManager
{
    protected List<String>                 cacheNames = new LinkedList<String>();
    protected ConcurrentMap<String, Cache> cacheMap   = new ConcurrentHashMap<String, Cache>();
    
    public AbstractCacheManager()
    {
        cacheNames.add("default");
    }
    
    @Override
    public Cache get(String name)
    {
        return cacheMap.get(name);
    }
    
}
