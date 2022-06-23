package com.jfireframework.jfire.bean.impl;

import java.util.HashMap;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.bean.field.dependency.DIField;
import com.jfireframework.jfire.bean.field.param.ParamField;

public class DefaultBean extends BaseBean
{
    
    public DefaultBean(Class<?> type, String beanName, boolean prototype, DIField[] diFields, ParamField[] paramFields)
    {
        super(type, beanName, prototype, diFields, paramFields);
    }
    
    @Override
    public Object getInstance()
    {
        HashMap<String, Object> map = beanInstanceMap.get();
        map.clear();
        return getInstance(map);
        
    }
    
    @Override
    public Object getInstance(Map<String, Object> beanInstanceMap)
    {
        if (beanInstanceMap.containsKey(beanName))
        {
            return beanInstanceMap.get(beanName);
        }
        if (prototype == false)
        {
            if (singletonInstance == null)
            {
                initSingletonInstance(beanInstanceMap);
                return singletonInstance;
            }
            else
            {
                return singletonInstance;
            }
        }
        else
        {
            return buildInstance(beanInstanceMap);
        }
    }
    
    private Object buildInstance(Map<String, Object> beanInstanceMap)
    {
        try
        {
            Object instance = type.newInstance();
            beanInstanceMap.put(beanName, instance);
            for (DIField each : diFields)
            {
                each.inject(instance, beanInstanceMap);
            }
            for (ParamField each : paramFields)
            {
                each.setParam(instance);
            }
            if (postConstructMethod != null)
            {
                postConstructMethod.invoke(instance, null);
            }
            return instance;
        }
        catch (Exception e)
        {
            throw new UnSupportException(StringUtil.format("初始化bean实例错误，实例名称:{},对象类名:{}", beanName, type.getName()), e);
        }
    }
    
    private synchronized void initSingletonInstance(Map<String, Object> beanInstanceMap)
    {
        try
        {
            if (singletonInstance == null)
            {
                Object instance = type.newInstance();
                beanInstanceMap.put(beanName, instance);
                for (DIField each : diFields)
                {
                    each.inject(instance, beanInstanceMap);
                }
                for (ParamField each : paramFields)
                {
                    each.setParam(instance);
                }
                if (postConstructMethod != null)
                {
                    postConstructMethod.invoke(instance, null);
                }
                singletonInstance = instance;
            }
        }
        catch (Exception e)
        {
            throw new UnSupportException(StringUtil.format("初始化bean实例错误，实例名称:{},对象类名:{}", beanName, type.getName()), e);
        }
    }
}
