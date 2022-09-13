package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.impl;

public class IntArrayResolver extends ObjectResolver
{
    @Override
    protected void initialize(String value)
    {
        String[] tmp = value.split(",");
        int[] array = new int[tmp.length];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = Integer.valueOf(tmp[i]);
        }
        this.value = array;
    }
}
