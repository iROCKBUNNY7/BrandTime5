package com.jfireframework.jfire.config.environment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.config.Condition;

public class Environment
{
    protected Map<Class<? extends Condition>, Condition> conditionImplStore  = new HashMap<Class<? extends Condition>, Condition>();
    protected Map<String, String>                        properties;
    protected Set<Class<?>>                              configClasses       = new HashSet<Class<?>>();
    protected Set<Method>                                configMethods       = new HashSet<Method>();
    protected final Map<String, BeanDefinition>          beanDefinitions;
    protected final JfireConfig                          jfireConfig;
    private final ReadOnlyEnvironment                    readOnlyEnvironment = new ReadOnlyEnvironment(this);
    private final AnnotationUtil                         annotationUtil      = new AnnotationUtil();
    
    public Environment(Map<String, BeanDefinition> beanDefinitions, Map<String, String> properties, JfireConfig jfireConfig)
    {
        this.beanDefinitions = beanDefinitions;
        this.properties = properties;
        this.jfireConfig = jfireConfig;
    }
    
    public void registerBeanDefinition(BeanDefinition beanDefinition)
    {
        jfireConfig.registerBeanDefinition(beanDefinition);
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
        
        public boolean isBeanDefinitionExist(String beanName)
        {
            return host.getBeanDefinition(beanName) != null;
        }
        
        public boolean isBeanDefinitionExist(Class<?> type)
        {
            return host.getBeanDefinition(type) != null;
        }
    }
    
    public ReadOnlyEnvironment readOnlyEnvironment()
    {
        return readOnlyEnvironment;
    }
    
    public void addConfigClass(Class<?> configClass)
    {
        configClasses.add(configClass);
    }
    
    public void addConfigMethod(Method method)
    {
        configMethods.add(method);
    }
    
    public boolean isAnnotationPresent(Class<? extends Annotation> annoType)
    {
        for (Class<?> each : configClasses)
        {
            if (annotationUtil.isPresent(annoType, each))
            {
                return true;
            }
        }
        for (Method each : configMethods)
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
        for (Class<?> each : configClasses)
        {
            if (annotationUtil.isPresent(type, each))
            {
                return annotationUtil.getAnnotation(type, each);
            }
        }
        for (Method each : configMethods)
        {
            if (annotationUtil.isPresent(type, each))
            {
                return annotationUtil.getAnnotation(type, each);
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T[] getAnnotations(Class<T> type)
    {
        List<T> list = new ArrayList<T>();
        for (Class<?> each : configClasses)
        {
            if (annotationUtil.isPresent(type, each))
            {
                for (T anno : annotationUtil.getAnnotations(type, each))
                {
                    list.add(anno);
                }
            }
        }
        for (Method each : configMethods)
        {
            if (annotationUtil.isPresent(type, each))
            {
                for (T anno : annotationUtil.getAnnotations(type, each))
                {
                    list.add(anno);
                }
            }
        }
        return list.toArray((T[]) Array.newInstance(type, list.size()));
    }
    
    public BeanDefinition getBeanDefinition(String beanName)
    {
        return beanDefinitions.get(beanName);
    }
    
    public BeanDefinition getBeanDefinition(Class<?> type)
    {
        for (BeanDefinition each : beanDefinitions.values())
        {
            if (type.isAssignableFrom(each.getOriginType()))
            {
                return each;
            }
        }
        return null;
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
    
    public Condition getCondition(Class<? extends Condition> ckass)
    {
        Condition instance = conditionImplStore.get(ckass);
        if (instance == null)
        {
            try
            {
                instance = ckass.newInstance();
                conditionImplStore.put(ckass, instance);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        return instance;
    }
    
    public AnnotationUtil getAnnotationUtil()
    {
        return annotationUtil;
    }
}
