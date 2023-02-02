package com.jfireframework.jfire.core.aop.support.cache;

import com.jfireframework.jfire.core.aop.impl.CacheAopManager.Cache;
import com.jfireframework.jfire.core.aop.impl.CacheAopManager.CacheManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractCacheManager implements CacheManager
{
    protected List<String> cacheNames = new LinkedList<String>();
    protected ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();

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
