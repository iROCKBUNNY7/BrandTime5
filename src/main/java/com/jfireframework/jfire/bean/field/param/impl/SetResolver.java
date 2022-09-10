package com.jfireframework.jfire.bean.field.param.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;

public class SetResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        if (type instanceof ParameterizedType)
        {
            type = ((ParameterizedType) type).getRawType();
        }
        if (type instanceof Class<?>)
        {
            if (type == String.class)
            {
                Set<String> set = new HashSet<String>();
                for (String each : value.split(","))
                {
                    set.add(each);
                }
                this.value = set;
            }
            else if (type == Integer.class)
            {
                Set<Integer> set = new HashSet<Integer>();
                for (String each : value.split(","))
                {
                    set.add(Integer.valueOf(each));
                }
                this.value = set;
            }
            else if (type == Long.class)
            {
                Set<Long> set = new HashSet<Long>();
                for (String each : value.split(","))
                {
                    set.add(Long.valueOf(each));
                }
                this.value = set;
            }
            else if (type == Float.class)
            {
                Set<Float> set = new HashSet<Float>();
                for (String each : value.split(","))
                {
                    set.add(Float.valueOf(each));
                }
                this.value = set;
            }
            else if (type == Double.class)
            {
                Set<Double> set = new HashSet<Double>();
                for (String each : value.split(","))
                {
                    set.add(Double.valueOf(each));
                }
                this.value = set;
            }
            else if (type == Class.class)
            {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Set<Class<?>> set = new HashSet<Class<?>>();
                for (String each : value.split(","))
                {
                    try
                    {
                        set.add(classLoader.loadClass(each));
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new JustThrowException(e);
                    }
                }
                this.value = set;
            }
            else
            {
                throw new UnSupportException("目前Set注入只支持String,Integer,Long,Float,Double,Class");
            }
        }
        else
        {
            throw new UnSupportException("Set注入，必须指明注入类型，而不能使用问号");
        }
    }
    
}