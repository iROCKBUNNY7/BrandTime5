package com.jfireframework.context.test.function.initmethod;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.codejson.JsonTool;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.config.JfireInitializationCfg;

public class InitMethodTest
{
    
    @Test
    public void test()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.initmethod");
        JfireConfig config = new JfireConfig(cfg);
        Person person = new Jfire(config).getBean(Person.class);
        Assert.assertEquals(23, person.getAge());
        Assert.assertEquals("林斌", person.getName());
    }
    
    @Test
    public void testcfg()
    {
        JfireInitializationCfg cfg = new JfireInitializationCfg();
        cfg.setScanPackageNames("com.jfireframework.context.test.function.initmethod");
        JfireConfig config = new JfireConfig(cfg);
        BeanDefinition beanInfo = new BeanDefinition();
        beanInfo.setBeanName("p2");
        beanInfo.setPostConstructMethod("initage");
        config.registerBeanDefinition(beanInfo);
        Jfire jfire = new Jfire(config);
        Person2 person2 = jfire.getBean(Person2.class);
        System.out.println("dsasdasd");
        Assert.assertEquals(12, person2.getAge());
    }
    
    @Test
    public void testfilecfg() throws URISyntaxException
    {
        JfireInitializationCfg cfg = (JfireInitializationCfg) JsonTool.read(JfireInitializationCfg.class, StringUtil.readFromClasspath("init.json", Charset.forName("utf8")));
        cfg.setScanPackageNames("com.jfireframework.context.test.function.initmethod");
        JfireConfig config = new JfireConfig(cfg);
        Person2 person2 = new Jfire(config).getBean(Person2.class);
        Assert.assertEquals(12, person2.getAge());
    }
}
