package com.jfireframework.context.test.function.map;

import com.jfireframework.baseutil.Order;
import com.jfireframework.jfire.core.inject.notated.MapKeyMethodName;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Resource
public class Host
{
    @Resource
    @MapKeyMethodName("getOrder")
    private Map<Integer, Order> map = new HashMap<Integer, Order>();

    @Resource
    private Map<String, Order> map2 = new HashMap<String, Order>();

    public Map<Integer, Order> getMap()
    {
        return map;
    }

    public Map<String, Order> getMap2()
    {
        return map2;
    }

}
