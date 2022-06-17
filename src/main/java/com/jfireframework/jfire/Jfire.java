package com.jfireframework.jfire;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.bean.Bean;

public class Jfire
{
    protected Map<String, Bean>   beanNameMap = new HashMap<String, Bean>();
    protected Map<Class<?>, Bean> beanTypeMap = new HashMap<Class<?>, Bean>();
    
    public Jfire(JfireConfig jfireConfig)
    {
        jfireConfig.initContext(this);
        beanNameMap = jfireConfig.beanNameMap;
        beanTypeMap = jfireConfig.beanTypeMap;
    }
    
    public Object getBean(String name)
    {
        Bean bean = beanNameMap.get(name);
        if (bean != null)
        {
            return bean.getInstance();
        }
        else
        {
            throw new UnSupportException("bean:" + name + "不存在");
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> src)
    {
        Bean bean = getBeanInfo(src);
        return (T) bean.getInstance();
    }
    
    public Bean getBeanInfo(Class<?> beanClass)
    {
        Bean bean = beanTypeMap.get(beanClass);
        if (bean != null)
        {
            return bean;
        }
        throw new UnSupportException("bean" + beanClass.getName() + "不存在");
    }
    
    public Bean getBeanInfo(String resName)
    {
        return beanNameMap.get(resName);
    }
    
    public Bean[] getBeanByAnnotation(Class<? extends Annotation> annotationType)
    {
        List<Bean> beans = new LinkedList<Bean>();
        for (Bean each : beanNameMap.values())
        {
            if (AnnotationUtil.isPresent(annotationType, each.getOriginType()))
            {
                beans.add(each);
            }
        }
        return beans.toArray(new Bean[beans.size()]);
    }
    
    public Bean[] getBeanByInterface(Class<?> type)
    {
        List<Bean> list = new LinkedList<Bean>();
        for (Bean each : beanNameMap.values())
        {
            if (type.isAssignableFrom(each.getOriginType()))
            {
                list.add(each);
            }
        }
        return list.toArray(new Bean[list.size()]);
    }
    
    /**
     * 关闭容器。该方法会触发单例bean上的close方法
     */
    public void close()
    {
        for (Bean each : beanNameMap.values())
        {
            each.close();
        }
    }
}
