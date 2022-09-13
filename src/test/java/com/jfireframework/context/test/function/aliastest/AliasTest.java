package com.jfireframework.context.test.function.aliastest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.support.jfireprepared.ComponentScan;
import com.jfireframework.jfire.support.jfireprepared.Configuration;

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
        Resource resource = annotationUtil.getAnnotation(Resource.class, AliasTest.class);
        Assert.assertTrue(resource.shareable());
        Method method = AliasTest.class.getMethod("take");
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
        JfireConfig jfireConfig = new JfireConfig(aliasCompopntScan.class);
        Jfire jfire = new Jfire(jfireConfig);
        SingleDemo demo = (SingleDemo) jfire.getBean("demo");
        assertFalse(demo == null);
    }
}
