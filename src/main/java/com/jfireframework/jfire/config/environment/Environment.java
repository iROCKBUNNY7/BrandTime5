package com.jfireframework.jfire.config.environment;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.util.EnvironmentUtil;

public class Environment
{
    protected Map<String, String>               properties;
    protected Set<Class<?>>                     configClasses = new HashSet<Class<?>>();
    protected final Map<String, BeanDefinition> beanDefinitions;
    
    public Environment(Map<String, BeanDefinition> beanDefinitions, Map<String, String> properties)
    {
        this.beanDefinitions = beanDefinitions;
        this.properties = properties;
    }
    
    public static class ReadOnlyEnvironment
    {
        private final Environment host;
        
        public ReadOnlyEnvironment(Environment host)
        {
            this.host = host;
        }
        
        public boolean isAnnotationPresent(Class<? extends Annotation> annoType)
        {
            return host.isAnnotationPresent(annoType);
        }
        
        public <T extends Annotation> T getAnnotation(Class<T> type)
        {
            return host.getAnnotation(type);
        }
        
        public String getProperty(String name)
        {
            return host.getProperty(name);
        }
        
    }
    
    public ReadOnlyEnvironment readOnlyEnvironment()
    {
        return new ReadOnlyEnvironment(this);
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
    
    public void putProperty(String name, String value)
    {
        properties.put(name, value);
    }
    
    public void removeProperty(String name)
    {
        properties.remove(name);
    }
}
