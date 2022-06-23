package com.jfireframework.jfire.bean.impl;

import java.util.HashMap;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.bean.Bean;
import com.jfireframework.jfire.bean.field.dependency.DIField;
import com.jfireframework.jfire.bean.field.param.ParamField;
import sun.reflect.MethodAccessor;

public class AnnotationConfigBean extends BaseBean
{
    /**
     * 宿主所在的bean
     */
    private final Bean           hostBean;
    private final MethodAccessor methodAccessor;
    
    public AnnotationConfigBean(Bean hostBean, MethodAccessor methodAccessor, Class<?> type, String beanName, boolean prototype)
    {
        super(type, beanName, prototype, new DIField[0], new ParamField[0]);
        this.hostBean = hostBean;
        this.methodAccessor = methodAccessor;
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
        if (prototype)
        {
            return buildInstance(beanInstanceMap);
        }
        else
        {
            if (singletonInstance == null)
            {
                initSingletonInstance(beanInstanceMap);
            }
            return singletonInstance;
        }
    }
    
    private Object buildInstance(Map<String, Object> beanInstanceMap)
    {
        try
        {
            Object instance = methodAccessor.invoke(hostBean.getInstance(beanInstanceMap), null);
            beanInstanceMap.put(beanName, instance);
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
                Object instance = methodAccessor.invoke(hostBean.getInstance(beanInstanceMap), null);
                beanInstanceMap.put(beanName, instance);
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
