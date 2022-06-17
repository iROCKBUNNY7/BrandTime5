package com.jfireframework.jfire.cache.support;

import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.codejson.JsonObject;
import com.jfireframework.codejson.JsonTool;
import com.jfireframework.jfire.cache.Cache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisStringCache implements Cache
{
    class Element
    {
        private String className;
        private String value;
        
        public Element(Object value)
        {
            className = value.getClass().getName();
            value = JsonTool.write(value);
        }
        
        public String getClassName()
        {
            return className;
        }
        
        public void setClassName(String className)
        {
            this.className = className;
        }
        
        public String getValue()
        {
            return value;
        }
        
        public void setValue(String value)
        {
            this.value = value;
        }
        
    }
    
    private final JedisPool jedisPool;
    
    public RedisStringCache(String ip, int port)
    {
        jedisPool = new JedisPool(ip, port);
    }
    
    @Override
    public void put(String key, Object value)
    {
        Jedis jedis = jedisPool.getResource();
        try
        {
            jedis.set(key, JsonTool.write(new Element(value)));
        }
        finally
        {
            jedis.close();
        }
    }
    
    @Override
    public void put(String key, Object value, int timeToLive)
    {
        Jedis jedis = jedisPool.getResource();
        try
        {
            jedis.setex(key, timeToLive, JsonTool.write(new Element(value)));
        }
        finally
        {
            jedis.close();
        }
    }
    
    @Override
    public Object get(String key)
    {
        Jedis jedis = jedisPool.getResource();
        try
        {
            String value = jedis.get(key);
            JsonObject jsonObject = (JsonObject) JsonTool.fromString(value);
            String classname = jsonObject.getWString("className");
            value = jsonObject.getWString("value");
            return JsonTool.read(Class.forName(classname), value);
        }
        catch (ClassNotFoundException e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            jedis.close();
        }
    }
    
    @Override
    public void remove(String key)
    {
        Jedis jedis = jedisPool.getResource();
        try
        {
            jedis.del(key);
        }
        finally
        {
            jedis.close();
        }
    }
    
    @Override
    public void clear()
    {
        Jedis jedis = jedisPool.getResource();
        try
        {
            jedis.flushDB();
        }
        finally
        {
            jedis.close();
        }
    }
    
}
