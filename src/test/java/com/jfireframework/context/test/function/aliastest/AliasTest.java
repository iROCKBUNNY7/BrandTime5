package com.jfireframework.context.test.function.aliastest;

import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.prepare.annotation.ComponentScan;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Testalis3(t = "sada")
public class AliasTest
{
    @Autowired(wiredName = "demo")
    private SingleDemo bi;

    @MyMethod(load = "ss")
    public void take()
    {
    }

    @Test
    public void test() throws NoSuchMethodException, SecurityException, NoSuchFieldException
    {
        AnnotationUtil annotationUtil = new AnnotationUtil();
        Resource       resource       = annotationUtil.getAnnotation(Resource.class, AliasTest.class);
        Assert.assertTrue(resource.shareable());
        Method     method     = AliasTest.class.getMethod("take");
        InitMethod initMethod = annotationUtil.getAnnotation(InitMethod.class, method);
        assertEquals("ss", initMethod.name());
        Field field = AliasTest.class.getDeclaredField("bi");
        resource = annotationUtil.getAnnotation(Resource.class, field);
        assertEquals("demo", resource.name());
    }

    @ComponentScan("com.jfireframework.context.test.function.aliastest")
    @Configuration
    public static class aliasCompopntScan
    {

    }

    @Test
    public void test2()
    {
        JfireBootstrap jfireConfig = new JfireBootstrap(aliasCompopntScan.class);
        Jfire          jfire       = jfireConfig.start();
        SingleDemo     demo        = jfire.getBean("demo");
        assertFalse(demo == null);
    }
}
