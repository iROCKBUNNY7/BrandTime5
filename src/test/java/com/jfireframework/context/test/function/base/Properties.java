package com.jfireframework.context.test.function.base;

import java.nio.charset.Charset;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.codejson.JsonTool;
import com.jfireframework.context.test.function.base.data.ImmutablePerson;
import com.jfireframework.context.test.function.base.data.PropertyReadData;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.config.JfireInitializationCfg;

public class Properties
{
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig((JfireInitializationCfg) JsonTool.read(JfireInitializationCfg.class, StringUtil.readFromClasspath("propertiestest.json", Charset.forName("utf8"))));
        Jfire jfire = new Jfire(jfireConfig);
        ImmutablePerson person = jfire.getBean(ImmutablePerson.class);
        Assert.assertEquals(12, person.getAge());
    }
    
    @Test
    public void test2()
    {
        JfireConfig jfireConfig = new JfireConfig((JfireInitializationCfg) JsonTool.read(JfireInitializationCfg.class, StringUtil.readFromClasspath("propertiestest.json", Charset.forName("utf8"))));
        jfireConfig.registerBeanDefinition(PropertyReadData.class.getName(), false, PropertyReadData.class);
        Jfire jfire = new Jfire(jfireConfig);
        PropertyReadData data = jfire.getBean(PropertyReadData.class);
        Assert.assertEquals(13, data.getAge());
        Assert.assertEquals(10, data.getAge1());
    }
}
