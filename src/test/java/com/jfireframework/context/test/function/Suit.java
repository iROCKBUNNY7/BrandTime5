package com.jfireframework.context.test.function;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.jfireframework.context.test.function.aliastest.AliasTest;
import com.jfireframework.context.test.function.aop.AopTest;
import com.jfireframework.context.test.function.base.ContextTest;
import com.jfireframework.context.test.function.base.Properties;
import com.jfireframework.context.test.function.base.maptest.MapTest;
import com.jfireframework.context.test.function.beanannotest.BeanAnnoTest;
import com.jfireframework.context.test.function.cachetest.CacheTest;
import com.jfireframework.context.test.function.initmethod.InitMethodTest;
import com.jfireframework.context.test.function.loader.HolderTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AliasTest.class, AopTest.class, ContextTest.class, MapTest.class, CacheTest.class, InitMethodTest.class, //
        HolderTest.class, Properties.class, com.jfireframework.context.test.function.map.MapTest.class, //
        BeanAnnoTest.class//
})
public class Suit
{
    
}
