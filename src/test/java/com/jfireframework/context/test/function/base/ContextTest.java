package com.jfireframework.context.test.function.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import javax.annotation.Resource;
import org.junit.Test;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.simplelog.ConsoleLogFactory;
import com.jfireframework.baseutil.simplelog.Logger;
import com.jfireframework.codejson.JsonObject;
import com.jfireframework.codejson.JsonTool;
import com.jfireframework.context.test.function.base.data.House;
import com.jfireframework.context.test.function.base.data.ImmutablePerson;
import com.jfireframework.context.test.function.base.data.MutablePerson;
import com.jfireframework.jfire.JfireInitFinish;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.bean.Bean;
import com.jfireframework.jfire.config.BeanInfo;

public class ContextTest
{
    private Logger logger = ConsoleLogFactory.getLogger();
    
    static
    {
        ConsoleLogFactory.addLoggerCfg("com.jfireframework.context", ConsoleLogFactory.TRACE);
    }
    
    /**
     * 测试构造方法,并且测试单例的正确性与否
     */
    @Test
    public void testConstruction()
    {
        JfireConfig jfireConfig = new JfireConfig().addPackageNames("com.jfireframework.context.test.function.base", null);
        baseTest(new Jfire(jfireConfig));
    }
    
    private void baseTest(Jfire jfire)
    {
        assertEquals(3, jfire.getBeanByAnnotation(Resource.class).length);
        ImmutablePerson immutablePerson = jfire.getBean(ImmutablePerson.class);
        ImmutablePerson person2 = (ImmutablePerson) jfire.getBean(ImmutablePerson.class.getName());
        assertEquals(immutablePerson, person2);
        MutablePerson mutablePerson = jfire.getBean(MutablePerson.class);
        MutablePerson mutablePerson2 = jfire.getBean(MutablePerson.class);
        assertNotEquals(mutablePerson, mutablePerson2);
        logger.debug(mutablePerson.getHome().getName());
        assertEquals(mutablePerson.getHome(), immutablePerson.getHome());
        assertEquals("林斌的房子", jfire.getBean(House.class).getName());
        assertEquals(1, jfire.getBeanByInterface(JfireInitFinish.class).length);
    }
    
    /**
     * 测试手动加入beanconfig,对对象的参数属性进行设置
     */
    @Test
    public void testParam()
    {
        JfireConfig jfireConfig = new JfireConfig().addPackageNames("com.jfireframework.context.test.function.base");
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setBeanName(ImmutablePerson.class.getName());
        beanInfo.putParam("name", "林斌");
        beanInfo.putParam("age", "25");
        beanInfo.putParam("boy", "true");
        beanInfo.putParam("arrays", "12,1212,1212121");
        jfireConfig.addBeanInfo(beanInfo);
        Jfire jfire = new Jfire(jfireConfig);
        testParam(jfire);
        assertEquals("林斌的房子", jfire.getBean(House.class).getName());
        ImmutablePerson person = jfire.getBean(ImmutablePerson.class);
        String[] arrays = person.getArrays();
        assertEquals("12", arrays[0]);
        assertEquals("1212", arrays[1]);
        assertEquals("1212121", arrays[2]);
    }
    
    private void testParam(Jfire jfire)
    {
        ImmutablePerson person = jfire.getBean(ImmutablePerson.class);
        assertEquals(person.getAge(), 25);
        assertEquals(person.getName(), "林斌");
        assertEquals(person.getBoy(), true);
    }
    
    @Test
    public void testDirect()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.addBean(House.class);
        jfireConfig.addBean(MutablePerson.class);
        jfireConfig.addBean(ImmutablePerson.class);
        baseTest(new Jfire(jfireConfig));
    }
    
    @Test
    public void testDirect2()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.addBean(House.class.getName(), false, House.class);
        jfireConfig.addBean(MutablePerson.class);
        jfireConfig.addBean(ImmutablePerson.class);
        baseTest(new Jfire(jfireConfig));
    }
    
    @Test
    public void testInit()
    {
        JfireConfig jfireContext = new JfireConfig().addPackageNames("com.jfireframework.context.test.function.base");
        assertEquals(1, new Jfire(jfireContext).getBeanByInterface(JfireInitFinish.class).length);
    }
    
    @Test
    public void testInit2()
    {
        JfireConfig config = new JfireConfig().addPackageNames("com.jfireframework.context.test.function.base");
        Jfire jfire = new Jfire(config);
        Bean bean = jfire.getBeanInfo(House.class);
        assertEquals("林斌的房子", ((House) bean.getInstance()).getName());
        bean = jfire.getBeanInfo(House.class.getName());
        assertEquals("林斌的房子", ((House) bean.getInstance()).getName());
    }
    
    @Test
    public void testConfig() throws URISyntaxException
    {
        JfireConfig config = new JfireConfig();
        config.readConfig((JsonObject) JsonTool.fromString(StringUtil.readFromClasspath("config.json", Charset.forName("utf8"))));
        Jfire jfire = new Jfire(config);
        baseTest(jfire);
        testParam(jfire);
    }
    
    @Test
    public void testConfig2() throws URISyntaxException
    {
        JfireConfig config = new JfireConfig();
        config.readConfig((JsonObject) JsonTool.fromString(StringUtil.readFromClasspath("config2.json", Charset.forName("utf8"))));
        Jfire jfire = new Jfire(config);
        baseTest(jfire);
        testParam(jfire);
    }
    
}
