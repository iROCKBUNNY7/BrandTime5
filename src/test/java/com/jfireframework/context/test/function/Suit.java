package com.jfireframework.context.test.function;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.jfireframework.context.test.function.aliastest.AliasTest;
import com.jfireframework.context.test.function.aop.AopTest;
import com.jfireframework.context.test.function.base.ContextTest;
import com.jfireframework.context.test.function.beanannotest.BeanAnnoTest;
import com.jfireframework.context.test.function.cachetest.CacheTest;
import com.jfireframework.context.test.function.initmethod.InitMethodTest;
import com.jfireframework.context.test.function.lazyinit.LazyInitTest;
import com.jfireframework.context.test.function.loader.HolderTest;
import com.jfireframework.context.test.function.map.MapTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ //
        AliasTest.class, AopTest.class, ParamFieldTest.class, //
        PropertyPathImporterTest.class, //
        DiTest.class, //
        ContextTest.class, CacheTest.class, InitMethodTest.class, //
        HolderTest.class, MapTest.class, //
        BeanAnnoTest.class, //
        LazyInitTest.class
})

public class Suit
{
    
}
