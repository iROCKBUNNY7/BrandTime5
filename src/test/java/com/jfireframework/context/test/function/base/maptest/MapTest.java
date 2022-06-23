package com.jfireframework.context.test.function.base.maptest;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.codejson.JsonTool;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.config.JfireInitializationCfg;

public class MapTest
{
    @Test
    public void test() throws URISyntaxException
    {
        JfireConfig config = new JfireConfig((JfireInitializationCfg) JsonTool.read(JfireInitializationCfg.class, StringUtil.readFromClasspath("mapconfig.json", Charset.forName("utf8"))));
        Jfire jfire = new Jfire(config);
        House house = jfire.getBean(House.class);
        Map<String, Person> map = house.getMap();
        Assert.assertEquals(2, map.size());
        
    }
}
