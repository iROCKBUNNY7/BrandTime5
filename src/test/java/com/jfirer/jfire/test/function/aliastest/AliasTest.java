package com.jfirer.jfire.test.function.aliastest;

import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
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
        AnnotationContextFactory annotationContextFactory = new SupportOverrideAttributeAnnotationContextFactory();
        Resource                 resource                 = annotationContextFactory.get(AliasTest.class, Thread.currentThread().getContextClassLoader()).getAnnotation(Resource.class);
        Assert.assertTrue(resource.shareable());
        Method     method     = AliasTest.class.getMethod("take");
        InitMethod initMethod = annotationContextFactory.get(method).getAnnotation(InitMethod.class);
        assertEquals("ss", initMethod.name());
        Field field = AliasTest.class.getDeclaredField("bi");
        resource = annotationContextFactory.get(field).getAnnotation(Resource.class);
        assertEquals("demo", resource.name());
    }

    @ComponentScan("com.jfirer.jfire.test.function.aliastest")
    @Configuration
    public static class aliasCompopntScan
    {

    }

    @Test
    public void test2()
    {
        ApplicationContext context = new DefaultApplicationContext(aliasCompopntScan.class);
        SingleDemo         demo    = context.getBean("demo");
        assertFalse(demo == null);
    }
}
