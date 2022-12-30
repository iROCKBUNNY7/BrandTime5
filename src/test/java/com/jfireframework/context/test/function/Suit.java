package com.jfireframework.context.test.function;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.jfireframework.context.test.function.aliastest.AliasTest;
import com.jfireframework.context.test.function.aop.AopTest;
import com.jfireframework.context.test.function.base.ContextTest;
import com.jfireframework.context.test.function.beanannotest.BeanAnnoTest;
import com.jfireframework.context.test.function.cachetest.CacheTest;
import com.jfireframework.context.test.function.initmethod.InitMethodTest;
import com.jfireframework.context.test.function.loader.HolderTest;
import com.jfireframework.context.test.function.map.MapTest;
import com.jfireframework.context.test.function.validate.ValidateTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ //
        AliasTest.class, //
        AopTest.class, //
        ContextTest.class, //
        BeanAnnoTest.class, //
        ParamFieldTest.class, //
        CacheTest.class, //
        InitMethodTest.class, //
        HolderTest.class, //
        MapTest.class, //
        ValidateTest.class, //
        ConfigBeanTest.class, //
        DiTest.class, //
        ParamFieldTest.class, //
        PropertyPathImporterTest.class, //
        StarterTest.class, //
})
public class Suit
{
    
}
