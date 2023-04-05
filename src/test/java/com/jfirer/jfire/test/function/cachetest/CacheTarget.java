package com.jfirer.jfire.test.function.cachetest;

import com.jfirer.baseutil.uniqueid.AutumnId;
import com.jfirer.jfire.core.aop.notated.cache.CacheDelete;
import com.jfirer.jfire.core.aop.notated.cache.CacheGet;
import com.jfirer.jfire.core.aop.notated.cache.CachePut;
import com.jfirer.jfire.test.function.base.data.House;
import com.jfirer.jfire.test.function.base.data.MutablePerson;

import javax.annotation.Resource;

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
