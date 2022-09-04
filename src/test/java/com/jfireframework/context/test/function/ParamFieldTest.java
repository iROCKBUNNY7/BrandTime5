package com.jfireframework.context.test.function;

import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.aware.JfireAwareBeforeInitialization;
import com.jfireframework.jfire.bean.annotation.field.PropertyRead;
import com.jfireframework.jfire.config.environment.Environment;

public class ParamFieldTest implements JfireAwareBeforeInitialization
{
    public static enum name
    {
        test1, test2
    }
    
    @PropertyRead
    private int[]       f1;
    @PropertyRead
    private String      f2;
    @PropertyRead
    private Integer     f3;
    @PropertyRead
    private int         f4;
    @PropertyRead
    private Long        f5;
    @PropertyRead
    private long        f6;
    @PropertyRead
    private Boolean     f7;
    @PropertyRead
    private boolean     f8;
    @PropertyRead
    private float       f9;
    @PropertyRead
    private Float       f10;
    @PropertyRead
    private String[]    f11;
    @PropertyRead
    private Set<String> f12;
    @PropertyRead
    private Class<?>    f13;
    @PropertyRead
    private name        f14;
    
    @Override
    public void awareBeforeInitialization(Environment environment)
    {
        environment.putProperty("f1", "1,2");
        environment.putProperty("f2", "aaa");
        environment.putProperty("f3", "1");
        environment.putProperty("f4", "2");
        environment.putProperty("f5", "3");
        environment.putProperty("f6", "4");
        environment.putProperty("f7", "true");
        environment.putProperty("f8", "false");
        environment.putProperty("f9", "2.65");
        environment.putProperty("f10", "2.35");
        environment.putProperty("f11", "ni,sx");
        environment.putProperty("f12", "xx,rr");
        environment.putProperty("f13", ParamFieldTest.class.getName());
        environment.putProperty("f14", "test1");
    }
    
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(ParamFieldTest.class);
        Jfire jfire = new Jfire(jfireConfig);
        ParamFieldTest data = jfire.getBean(ParamFieldTest.class);
        Assert.assertArrayEquals(new int[] { 1, 2 }, data.f1);
        Assert.assertEquals("aaa", data.f2);
        Assert.assertEquals(1, data.f3.intValue());
        Assert.assertEquals(2, data.f4);
        Assert.assertEquals(3, data.f5.intValue());
        Assert.assertEquals(4, data.f6);
        Assert.assertTrue(data.f7);
        Assert.assertFalse(data.f8);
        Assert.assertEquals(2.65, data.f9, 0.001);
        Assert.assertEquals(2.35, data.f10.floatValue(), 0.001);
        Assert.assertArrayEquals(new String[] { "ni", "sx" }, data.f11);
        Set<String> set = new HashSet<String>();
        set.add("xx");
        set.add("rr");
        Assert.assertEquals(set, data.f12);
        Assert.assertEquals(ParamFieldTest.class, data.f13);
        Assert.assertEquals(name.test1, data.f14);
    }
    
}
