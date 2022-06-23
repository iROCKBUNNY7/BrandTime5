package com.jfireframework.jfire;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.util.EnvironmentUtil;

public class Jfire
{
    protected Map<String, BeanDefinition> beanDefinitions = new HashMap<String, BeanDefinition>();
    
    public Jfire(JfireConfig jfireConfig)
    {
        jfireConfig.initJfire(this);
        beanDefinitions = jfireConfig.beanDefinitions;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name)
    {
        BeanDefinition beanDefinition = beanDefinitions.get(name);
        if (beanDefinition != null)
        {
            return (T) beanDefinition.getInstance();
        }
        else
        {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> src)
    {
        return (T) getBeanDefinition(src).getConstructedBean().getInstance();
    }
    
    public BeanDefinition getBeanDefinition(Class<?> beanClass)
    {
        for (BeanDefinition each : beanDefinitions.values())
        {
            if (each.getOriginType() == beanClass)
            {
                return each;
            }
        }
        throw new UnSupportException("bean" + beanClass.getName() + "不存在");
    }
    
    public BeanDefinition getBeanDefinition(String resName)
    {
        return beanDefinitions.get(resName);
    }
    
    public BeanDefinition[] getBeanDefinitionByAnnotation(Class<? extends Annotation> annotationType)
    {
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        List<BeanDefinition> result = new LinkedList<BeanDefinition>();
        for (BeanDefinition each : beanDefinitions.values())
        {
            if (annotationUtil.isPresent(annotationType, each.getOriginType()))
            {
                result.add(each);
            }
        }
        annotationUtil.clear();
        return result.toArray(new BeanDefinition[result.size()]);
    }
    
    public BeanDefinition[] getBeanDefinitionByInterface(Class<?> type)
    {
        List<BeanDefinition> list = new LinkedList<BeanDefinition>();
        for (BeanDefinition each : beanDefinitions.values())
        {
            if (type.isAssignableFrom(each.getOriginType()))
            {
                list.add(beanDefinitions.get(each.getBeanName()));
            }
        }
        return list.toArray(new BeanDefinition[list.size()]);
    }
    
    /**
     * 关闭容器。该方法会触发单例bean上的close方法
     */
    public void close()
    {
        for (BeanDefinition each : beanDefinitions.values())
        {
            each.getConstructedBean().close();
        }
    }
}
