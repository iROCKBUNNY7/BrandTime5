package com.jfirer.jfire.test.function;

import com.jfirer.jfire.test.function.aliastest.AliasTest;
import com.jfirer.jfire.test.function.aop.AopTest;
import com.jfirer.jfire.test.function.base.ContextTest;
import com.jfirer.jfire.test.function.beanannotest.BeanAnnoTest;
import com.jfirer.jfire.test.function.cachetest.CacheTest;
import com.jfirer.jfire.test.function.initmethod.InitMethodTest;
import com.jfirer.jfire.test.function.loader.HolderTest;
import com.jfirer.jfire.test.function.map.MapTest;
import com.jfirer.jfire.test.function.validate.ValidateTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

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
        ConfigurationOrderTest.class,//
        MapTest.class, //
        ValidateTest.class, //
        ConfigBeanTest.class, //
        DiTest.class, //
        ParamFieldTest.class, //
        PropertyPathImporterTest.class, //
        StarterTest.class, //
        AnnodatationDatabaseTest.class,//
})
public class Coverage
{

}
