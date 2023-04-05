package com.jfirer.jfire.test.function.map;


import javax.annotation.Resource;

@Resource
public class Order2 implements Order
{

    @Override
    public int getOrder()
    {
        return 2;
    }
}
