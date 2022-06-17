package com.jfireframework.jfire.cache;

public interface Cache
{
    public void put(String key, Object value);
    
    public void put(String key, Object value, int timeToLive);
    
    public Object get(String key);
    
    public void remove(String key);
    
    public void clear();
    
}
