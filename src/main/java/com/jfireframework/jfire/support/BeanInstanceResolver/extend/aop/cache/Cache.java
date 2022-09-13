package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.cache;

public interface Cache
{
    public void put(String key, Object value);
    
    public void put(String key, Object value, int timeToLive);
    
    public Object get(String key);
    
    public void remove(String key);
    
    public void clear();
    
}
