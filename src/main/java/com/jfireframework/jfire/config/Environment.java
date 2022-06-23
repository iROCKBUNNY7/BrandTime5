package com.jfireframework.jfire.config;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.util.EnvironmentUtil;

public class Environment
{
    private Map<String, String>               properties;
    private Set<Class<?>>                     configClasses = new HashSet<Class<?>>();
    private final Map<String, BeanDefinition> beanDefinitions;
    
    public Environment(Map<String, BeanDefinition> beanDefinitions, Map<String, String> properties)
    {
        this.beanDefinitions = beanDefinitions;
        this.properties = properties;
    }
    
    public void addConfigClass(Class<?> configClass)
    {
        configClasses.add(configClass);
    }
    
    public boolean isAnnotationPresent(Class<? extends Annotation> annoType)
    {
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        for (Class<?> each : configClasses)
        {
            if (annotationUtil.isPresent(annoType, each))
            {
                return true;
            }
        }
        return false;
    }
    
    public <T extends Annotation> T getAnnotation(Class<T> type)
    {
        AnnotationUtil annotationUtil = EnvironmentUtil.getAnnoUtil();
        for (Class<?> each : configClasses)
        {
            if (annotationUtil.isPresent(type, each))
            {
                return annotationUtil.getAnnotation(type, each);
            }
        }
        return null;
    }
    
    public BeanDefinition getBeanDefinition(String beanName)
    {
        return beanDefinitions.get(beanName);
    }
    
    public String getProperty(String name)
    {
        return properties.get(name);
    }
}
