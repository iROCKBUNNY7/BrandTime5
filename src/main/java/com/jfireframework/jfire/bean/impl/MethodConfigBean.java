package com.jfireframework.jfire.bean.impl;

import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.bean.Bean;
import sun.reflect.MethodAccessor;

public class MethodConfigBean extends BaseBean
{
    /**
     * 宿主所在的bean
     */
    private final Bean           hostBean;
    private final MethodAccessor methodAccessor;
    
    public MethodConfigBean(Bean hostBean, MethodAccessor methodAccessor, Class<?> type, String beanName, boolean prototype, boolean lazyInitUntilFirstInvoke)
    {
        super(type, beanName, prototype, lazyInitUntilFirstInvoke);
        this.hostBean = hostBean;
        this.methodAccessor = methodAccessor;
    }
    
    @Override
    protected Object buildInstance(Map<String, Object> beanInstanceMap)
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
    
}
