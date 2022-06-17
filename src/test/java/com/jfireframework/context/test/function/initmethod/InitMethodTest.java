package com.jfireframework.context.test.function.initmethod;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.codejson.JsonObject;
import com.jfireframework.codejson.JsonTool;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.config.BeanInfo;

public class InitMethodTest
{
    
    @Test
    public void test()
    {
        JfireConfig config = new JfireConfig().addPackageNames("com.jfireframework.context.test.function.initmethod");
        Person person = new Jfire(config).getBean(Person.class);
        Assert.assertEquals(23, person.getAge());
        Assert.assertEquals("林斌", person.getName());
    }
    
    @Test
    public void testcfg()
    {
        JfireConfig config = new JfireConfig().addPackageNames("com.jfireframework.context.test.function.initmethod");
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setBeanName("p2");
        beanInfo.setPostConstructMethod("initage");
        config.addBeanInfo(beanInfo);
        Jfire jfire = new Jfire(config);
        Person2 person2 = jfire.getBean(Person2.class);
        System.out.println("dsasdasd");
        Assert.assertEquals(12, person2.getAge());
    }
    
    @Test
    public void testfilecfg() throws URISyntaxException
    {
        JfireConfig config = new JfireConfig().addPackageNames("com.jfireframework.context.test.function.initmethod");
        config.readConfig((JsonObject) JsonTool.fromString(StringUtil.readFromClasspath("init.json", Charset.forName("utf8"))));
        Person2 person2 = new Jfire(config).getBean(Person2.class);
        Assert.assertEquals(12, person2.getAge());
    }
}
