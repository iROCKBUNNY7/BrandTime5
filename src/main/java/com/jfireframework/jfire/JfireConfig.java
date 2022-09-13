package com.jfireframework.jfire;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.ExtraInfoStore;
import com.jfireframework.jfire.kernel.JfireKernel;
import com.jfireframework.jfire.support.BeanInstanceResolver.LoadByBeanInstanceResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.LoadByBeanInstanceResolver.LoadBy;
import com.jfireframework.jfire.support.BeanInstanceResolver.OutterBeanInstanceResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.ReflectBeanInstanceResolver;
import com.jfireframework.jfire.support.jfireprepared.Configuration;
import com.jfireframework.jfire.support.jfireprepared.Import;
import com.jfireframework.jfire.support.jfireprepared.SelectImport;

public class JfireConfig
{
    protected Map<String, BeanDefinition> beanDefinitions = new HashMap<String, BeanDefinition>();
    protected ClassLoader                 classLoader     = JfireConfig.class.getClassLoader();
    protected Map<String, String>         properties      = new HashMap<String, String>();
    protected ExtraInfoStore              extraInfoStore  = new ExtraInfoStore();
    protected Environment                 environment     = new Environment(beanDefinitions, properties, this);
    protected AnnotationUtil              annotationUtil  = environment.getAnnotationUtil();
    protected static final Logger         logger          = LoggerFactory.getLogger(JfireConfig.class);
    
    public JfireConfig()
    {
    }
    
    public JfireConfig(Class<?> configClass)
    {
        if (annotationUtil.isPresent(Configuration.class, configClass))
        {
            environment.addConfigClass(configClass);
        }
        if (annotationUtil.isPresent(Resource.class, configClass))
        {
            registerBeanDefinition(configClass);
        }
    }
    
    public Environment getEnvironment()
    {
        return environment;
    }
    
    public JfireConfig registerBeanDefinition(Class<?>... ckasses)
    {
        for (Class<?> ckass : ckasses)
        {
            buildBeanDefinition(ckass);
        }
        return this;
    }
    
    public JfireConfig registerBeanDefinition(String resourceName, boolean prototype, Class<?> src)
    {
        buildBeanDefinition(resourceName, prototype, src);
        return this;
    }
    
    public JfireConfig registerBeanDefinition(BeanDefinition... definitions)
    {
        for (BeanDefinition definition : definitions)
        {
            beanDefinitions.put(definition.getBeanName(), definition);
        }
        return this;
    }
    
    protected void initJfire(Jfire jfire)
    {
        environment.setClassLoader(classLoader);
        registerSingletonEntity(ExtraInfoStore.class.getName(), extraInfoStore);
        registerSingletonEntity(Jfire.class.getName(), jfire);
        registerSingletonEntity(ClassLoader.class.getName(), classLoader);
        registerSingletonEntity(Environment.class.getName(), environment);
        registerBeanDefinition(Import.ProcessImport.class);
        registerBeanDefinition(SelectImport.ProcessSelectImport.class);
        registerBeanDefinition(Configuration.ProcessConfiguration.class);
        JfireKernel jfireKernel = new JfireKernel();
        jfireKernel.initJfire(environment, beanDefinitions, properties, classLoader, extraInfoStore);
    }
    
    public JfireConfig setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
        Thread.currentThread().setContextClassLoader(classLoader);
        return this;
    }
    
    public JfireConfig addProperties(Properties... properties)
    {
        for (Properties each : properties)
        {
            for (Entry<Object, Object> entry : each.entrySet())
            {
                this.properties.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
        return this;
    }
    
    public JfireConfig registerSingletonEntity(String beanName, Object entity)
    {
        BeanDefinition beanDefinition = new BeanDefinition(beanName, entity.getClass(), false, new OutterBeanInstanceResolver(beanName, entity.getClass(), entity));
        beanDefinitions.put(beanName, beanDefinition);
        return this;
    }
    
    private BeanDefinition buildBeanDefinition(String beanName, boolean prototype, Class<?> ckass)
    {
        BeanDefinition beanDefinition;
        if (annotationUtil.isPresent(LoadBy.class, ckass))
        {
            BeanInstanceResolver resolver = new LoadByBeanInstanceResolver(ckass, beanName, prototype);
            beanDefinition = new BeanDefinition(beanName, ckass, prototype, resolver);
        }
        else if (ckass.isInterface() == false)
        {
            BeanInstanceResolver resolver = new ReflectBeanInstanceResolver(beanName, ckass, prototype, classLoader, extraInfoStore, properties);
            beanDefinition = new BeanDefinition(beanName, ckass, prototype, resolver);
        }
        else
        {
            throw new UnSupportException(StringUtil.format("在接口上只有Resource注解是无法实例化bean的.请检查{}", ckass.getName()));
        }
        beanDefinitions.put(beanName, beanDefinition);
        return beanDefinition;
    }
    
    private BeanDefinition buildBeanDefinition(Class<?> ckass)
    {
        Resource resource = annotationUtil.getAnnotation(Resource.class, ckass);
        String beanName;
        boolean prototype;
        if (resource == null)
        {
            prototype = false;
            beanName = ckass.getName();
        }
        else
        {
            prototype = resource.shareable() == false;
            beanName = resource.name().equals("") ? ckass.getName() : resource.name();
        }
        return buildBeanDefinition(beanName, prototype, ckass);
    }
    
}
