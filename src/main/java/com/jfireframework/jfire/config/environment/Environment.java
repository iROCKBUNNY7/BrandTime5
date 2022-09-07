package com.jfireframework.jfire.config.environment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.condition.Condition;

public class Environment
{
    protected final Map<String, BeanDefinition>                beanDefinitions;
    protected final Map<String, String>                        properties;
    protected final JfireConfig                                jfireConfig;
    protected final Set<Method>                                configMethods       = new HashSet<Method>();
    protected final Set<Class<?>>                              configClasses       = new HashSet<Class<?>>();
    protected final Map<Class<? extends Condition>, Condition> conditionImplStore  = new HashMap<Class<? extends Condition>, Condition>();
    private final ReadOnlyEnvironment                          readOnlyEnvironment = new ReadOnlyEnvironment(this);
    private final AnnotationUtil                               annotationUtil      = new AnnotationUtil();
    private ClassLoader                                        classLoader;
    
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
    
    public void registerSingletonEntity(String beanName, Object entity)
    {
        jfireConfig.registerSingletonEntity(beanName, entity);
    }
    
    public void registerBeanDefinition(Class<?> ckass)
    {
        jfireConfig.registerBeanDefinition(ckass);
    }
    
    public void registerConfiurationBeanDefinition(Class<?> ckass)
    {
        jfireConfig.registerConfiurationBeanDefinition(ckass);
    }
    
    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }
    
    public ClassLoader getClassLoader()
    {
        return classLoader;
    }
    
    public static class ReadOnlyEnvironment
    {
        private final Environment host;
        
        public ReadOnlyEnvironment(Environment host)
        {
            this.host = host;
        }
        
        public Collection<BeanDefinition> beanDefinitions()
        {
            return host.beanDefinitions.values();
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
        
        public boolean hasProperty(String name)
        {
            return host.getProperty(name) != null;
        }
        
        public boolean isBeanDefinitionExist(String beanName)
        {
            return host.getBeanDefinition(beanName) != null;
        }
        
        public boolean isBeanDefinitionExist(Class<?> type)
        {
            return host.getBeanDefinition(type) != null;
        }
        
        public AnnotationUtil getAnnotationUtil()
        {
            return host.getAnnotationUtil();
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
