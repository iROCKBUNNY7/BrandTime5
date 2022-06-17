package com.jfireframework.jfire.cache.support;

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
