package com.jfireframework.jfire.bean.impl;

import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.bean.field.dependency.DIField;
import com.jfireframework.jfire.bean.field.param.ParamField;

public class DefaultBean extends BaseBean
{
    
    public DefaultBean(Class<?> type, String beanName, boolean prototype, DIField[] diFields, ParamField[] paramFields, boolean lazyInitUntilFirstInvoke)
    {
        super(type, beanName, prototype, diFields, paramFields, lazyInitUntilFirstInvoke);
    }
    
    @Override
    protected Object buildInstance(Map<String, Object> beanInstanceMap)
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
}
