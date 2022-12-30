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
    
    @CacheGet(value = "'ab'", cacheName = "name", condition = "person.age > 13")
    public House get4(MutablePerson person)
    {
        return new House();
    }
    
    @CacheGet(value = "'ab'+id", cacheName = "name", condition = "id >= 6 && id <= 9")
    public House get3(int id)
    {
        return new House();
    }
    
    @CacheGet(value = "'ab'+id", cacheName = "name", condition = "id >= 6")
    public House get2(int id)
    {
        return new House();
    }
    
    @CacheGet(value = "'ab'+id", cacheName = "name", condition = "id > 4")
    public House get(int id)
    {
        System.out.println("调用");
        return new House();
    }
    
    @CachePut(value = "'ab'+id", cacheName = "name", condition = "id> 2")
    public House put(int id)
    {
        return new House();
    }
    
    @CacheDelete(value = "'ab'+id", cacheName = "name")
    public void remove(int id)
    {
        ;
    }
    
    @CacheGet("'abclist'")
    public String get()
    {
        System.out.println("setarray");
        return AutumnId.instance().generate();
    }
    
    @CachePut("'abc'")
    @CacheDelete("'abclist'")
    public String put()
    {
        System.out.println("put");
        return "abc";
    }
}
