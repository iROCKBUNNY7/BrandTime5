package com.jfireframework.jfire.core.aop.support.cache;

import javax.annotation.PostConstruct;

public class ConcurrentMapCacheManager extends AbstractCacheManager
{

    @PostConstruct
    public void init()
    {
        for (String name : cacheNames)
        {
            ConcurrentMapCache cache = new ConcurrentMapCache();
            cacheMap.put(name, cache);
        }
    }
}
