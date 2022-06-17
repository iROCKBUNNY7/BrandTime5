package com.jfireframework.context.test.function.event;

import java.util.HashMap;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.config.BeanInfo;
import com.jfireframework.jfire.coordinator.CoordinatorRegisterHelper;

public class EventTest
{
    @Test
    public void test()
    {
        JfireConfig config = new JfireConfig();
        config.addBean(HaftHandler.class);
        config.addBean("eventregisterhelper", false, CoordinatorRegisterHelper.class);
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setBeanName("eventregisterhelper");
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("path", "com.jfireframework.context.test.function.event");
        beanInfo.setParams(params);
        config.addBeanInfo(beanInfo);
        new Jfire(config);
    }
}
