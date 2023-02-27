package com.jfireframework.context.test.function.cachetest;

import com.jfireframework.jfire.core.aop.impl.CacheAopManager.Cache;

import java.util.HashMap;

public class DemoCache implements Cache
{
    private HashMap<Object, Object> map = new HashMap<Object, Object>();

    @Override
    public Object get(String key)
    {
        return map.get(key);
    }

    @Override
    public void remove(String key)
    {
        map.remove(key);
    }

    @Override
    public void clear()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void put(String key, Object value, int timeToLive)
    {
        map.put(key, value);
    }
}
