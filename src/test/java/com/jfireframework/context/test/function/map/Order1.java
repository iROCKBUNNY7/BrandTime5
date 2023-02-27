package com.jfireframework.context.test.function.map;

import com.jfireframework.baseutil.Order;

import javax.annotation.Resource;

@Resource
public class Order1 implements Order
{

    @Override
    public int getOrder()
    {
        return 1;
    }
}
