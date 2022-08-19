package com.jfireframework.jfire.bean.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.bean.Bean;

public class MethodConfigBean extends BaseBean
{
    /**
     * 宿主所在的bean
     */
    private final Bean   hostBean;
    private final Method method;
    private final Bean[] paramBeans;
    
    public MethodConfigBean(Bean hostBean, List<Bean> paramBeans, Method method, Class<?> type, String beanName, boolean prototype, boolean lazyInitUntilFirstInvoke)
    {
        super(type, beanName, prototype, lazyInitUntilFirstInvoke);
        this.hostBean = hostBean;
        this.method = method;
        this.paramBeans = paramBeans.toArray(new Bean[paramBeans.size()]);
    }
    
    @Override
    protected Object buildInstance(Map<String, Object> beanInstanceMap)
    {
        try
        {
            Object host = hostBean.getInstance(beanInstanceMap);
            Object[] params = new Object[paramBeans.length];
            for (int i = 0; i < params.length; i++)
            {
                params[i] = paramBeans[i].getInstance(beanInstanceMap);
            }
            Object instance = method.invoke(host, params);
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
