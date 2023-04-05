package com.jfirer.jfire.core.aop.support.cache;

import com.jfirer.jfire.core.aop.impl.CacheAopManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractCacheManager implements CacheAopManager.CacheManager
{
    protected List<String>                                 cacheNames = new LinkedList<String>();
    protected ConcurrentMap<String, CacheAopManager.Cache> cacheMap   = new ConcurrentHashMap<String, CacheAopManager.Cache>();

    public AbstractCacheManager()
    {
        cacheNames.add("default");
    }

    @Override
    public CacheAopManager.Cache get(String name)
    {
        return cacheMap.get(name);
    }
}
