package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache.support;

import javax.annotation.PostConstruct;

public class RedisStringCacheManager extends AbstractCacheManager
{
    private String ip;
    private int    port;
    
    @PostConstruct
    public void init()
    {
        for (String name : cacheNames)
        {
            RedisStringCache cache = new RedisStringCache(ip, port);
            cacheMap.put(name, cache);
        }
    }
    
}
