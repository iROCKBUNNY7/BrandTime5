package com.jfireframework.context.test.function.cachetest;

import javax.annotation.Resource;
import com.jfireframework.baseutil.uniqueid.AutumnId;
import com.jfireframework.context.test.function.base.data.House;
import com.jfireframework.context.test.function.base.data.MutablePerson;
import com.jfireframework.jfire.core.aop.notated.cache.CacheDelete;
import com.jfireframework.jfire.core.aop.notated.cache.CacheGet;
import com.jfireframework.jfire.core.aop.notated.cache.CachePut;

@Resource
public class CacheTarget
{
    
    @CacheGet(value = "\"ab\"", cacheName = "name", condition = "$0.age > 13")
    public House get4(MutablePerson person)
    {
        return new House();
    }
    
    @CacheGet(value = "\"ab\"+$0", cacheName = "name", condition = "$0 >= 6 && $0 <= 9")
    public House get3(int id)
    {
        return new House();
    }
    
    @CacheGet(value = "\"ab\"+$0", cacheName = "name", condition = "$0 >= 6")
    public House get2(int id)
    {
        return new House();
    }
    
    @CacheGet(value = "\"ab\"+$0", cacheName = "name", condition = "$0 > 4")
    public House get(int id)
    {
        System.out.println("调用");
        return new House();
    }
    
    @CachePut(value = "\"ab\"+$0", cacheName = "name", condition = "$0 > 2")
    public House put(int id)
    {
        return new House();
    }
    
    @CacheDelete(value = "\"ab\"+$0", cacheName = "name")
    public void remove(int id)
    {
        ;
    }
    
    @CacheGet("\"abclist\"")
    public String get()
    {
        System.out.println("setarray");
        return AutumnId.instance().generate();
    }
    
    @CachePut("\"abc\"")
    @CacheDelete("\"abclist\"")
    public String put()
    {
        System.out.println("put");
        return "abc";
    }
}
