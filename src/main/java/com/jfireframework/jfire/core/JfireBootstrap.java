package com.jfireframework.jfire.core;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import javax.annotation.Resource;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.jfire.core.aop.AopManager;
import com.jfireframework.jfire.core.aop.AopManagerNotated;
import com.jfireframework.jfire.core.aop.impl.CacheAopManager;
import com.jfireframework.jfire.core.aop.impl.DefaultAopManager;
import com.jfireframework.jfire.core.aop.impl.TransactionAopManager;
import com.jfireframework.jfire.core.aop.impl.ValidateAopManager;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.core.prepare.impl.AddProperty.AddPropertyProcessor;
import com.jfireframework.jfire.core.prepare.impl.ComponentScan.ComponentScanProcessor;
import com.jfireframework.jfire.core.prepare.impl.Configuration.ConfigurationProcessor;
import com.jfireframework.jfire.core.prepare.impl.EnableAutoConfiguration.EnableAutoConfigurationProcessor;
import com.jfireframework.jfire.core.prepare.impl.Import.ImportProcessor;
import com.jfireframework.jfire.core.prepare.impl.ProfileSelector.ProfileSelectorProcessor;
import com.jfireframework.jfire.core.prepare.impl.PropertyPath.PropertyPathProcessor;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.OutterObjectBeanInstanceResolver;
import com.jfireframework.jfire.exception.NewBeanInstanceException;
import com.jfireframework.jfire.util.Utils;

public class JfireBootstrap
{
    private Environment environment = new Environment();
    
    public JfireBootstrap()
    {
    }
    
    public JfireBootstrap(Class<?> configClass)
    {
        environment.addAnnotations(configClass);
    }
    
    public void addAnnotations(Class<?> ckass)
    {
        environment.addAnnotations(ckass);
    }
    
    public void addAnnotations(Method method)
    {
        environment.addAnnotations(method);
    }
    
    public Jfire start()
    {
        TRACEID.newTraceId();
        Jfire jfire = registerJfireInstance();
        registerDefault();
        prepare(environment);
        aopScan(environment);
        invokeBeanDefinitionInitMethod(environment);
        awareContextInit(environment);
        return jfire;
    }
    
    private void registerDefault()
    {
        registerDirect(ImportProcessor.class);
        registerDirect(DefaultAopManager.class);
        registerDirect(TransactionAopManager.class);
        registerDirect(CacheAopManager.class);
        registerDirect(ValidateAopManager.class);
        /**/
        registerDirect(AddPropertyProcessor.class);
        registerDirect(ComponentScanProcessor.class);
        registerDirect(ConfigurationProcessor.class);
        registerDirect(EnableAutoConfigurationProcessor.class);
        registerDirect(ProfileSelectorProcessor.class);
        registerDirect(PropertyPathProcessor.class);
    }
    
    private Jfire registerJfireInstance()
    {
        Jfire jfire = new Jfire(environment);
        BeanDefinition beanDefinition = new BeanDefinition(Jfire.class.getName(), Jfire.class, false);
        BeanInstanceResolver resolver = new OutterObjectBeanInstanceResolver(jfire);
        beanDefinition.setBeanInstanceResolver(resolver);
        register(beanDefinition);
        return jfire;
    }
    
    private void awareContextInit(Environment environment)
    {
        for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
        {
            if (beanDefinition.isAwareContextInit())
            {
                ((JfireAwareContextInited) beanDefinition.getBeanInstance()).awareContextInited(environment.readOnlyEnvironment());
            }
        }
    }
    
    private void invokeBeanDefinitionInitMethod(Environment environment)
    {
        for (Entry<String, BeanDefinition> entry : environment.beanDefinitions().entrySet())
        {
            entry.getValue().init(environment);
        }
    }
    
    private void prepare(Environment environment)
    {
        PriorityQueue<BeanDefinition> queue = new PriorityQueue<BeanDefinition>(environment.beanDefinitions().size(), new Comparator<BeanDefinition>() {
            
            @Override
            public int compare(BeanDefinition o1, BeanDefinition o2)
            {
                int order1 = Utils.ANNOTATION_UTIL.getAnnotation(JfirePreparedNotated.class, o1.getType()).order();
                int order2 = Utils.ANNOTATION_UTIL.getAnnotation(JfirePreparedNotated.class, o2.getType()).order();
                return order1 - order2;
            }
        });
        List<String> deleteBeanNames = new LinkedList<String>();
        do
        {
            deleteBeanNames.clear();
            environment.markVersion();
            for (Entry<String, BeanDefinition> entry : environment.beanDefinitions().entrySet())
            {
                if (Utils.ANNOTATION_UTIL.isPresent(JfirePreparedNotated.class, entry.getValue().getType()) && JfirePrepare.class.isAssignableFrom(entry.getValue().getType()))
                {
                    queue.add(entry.getValue());
                    deleteBeanNames.add(entry.getKey());
                }
            }
            for (String each : deleteBeanNames)
            {
                environment.removeBeanDefinition(each);
            }
            BeanDefinition minOrderedJfirePrepare = queue.poll();
            if (minOrderedJfirePrepare != null)
            {
                try
                {
                    JfirePrepare jfirePrepareInstance = (JfirePrepare) minOrderedJfirePrepare.getType().newInstance();
                    jfirePrepareInstance.prepare(environment);
                }
                catch (Throwable e)
                {
                    throw new NewBeanInstanceException(e);
                }
            }
        } while (queue.isEmpty() == false || environment.isChanged());
    }
    
    private void aopScan(Environment environment)
    {
        List<BeanDefinition> list = new LinkedList<BeanDefinition>();
        for (Entry<String, BeanDefinition> each : environment.beanDefinitions().entrySet())
        {
            if (AopManager.class.isAssignableFrom(each.getValue().getType()) && Utils.ANNOTATION_UTIL.isPresent(AopManagerNotated.class, each.getValue().getType()))
            {
                list.add(each.getValue());
            }
        }
        for (BeanDefinition each : list)
        {
            environment.removeBeanDefinition(each.getBeanName());
        }
        for (BeanDefinition each : list)
        {
            try
            {
                AopManager instance = (AopManager) each.getType().newInstance();
                instance.scan(environment);
            }
            catch (Exception e)
            {
                throw new NewBeanInstanceException(e);
            }
        }
    }
    
    public void register(BeanDefinition beanDefinition)
    {
        beanDefinition.check();
        environment.registerBeanDefinition(beanDefinition);
    }
    
    public void register(Class<?> ckass)
    {
        if (Utils.ANNOTATION_UTIL.isPresent(Resource.class, ckass))
        {
            Resource resource = Utils.ANNOTATION_UTIL.getAnnotation(Resource.class, ckass);
            String beanName = StringUtil.isNotBlank(resource.name()) ? resource.name() : ckass.getName();
            boolean prototype = !resource.shareable();
            BeanDefinition beanDefinition = new BeanDefinition(beanName, ckass, prototype);
            beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(ckass));
            environment.registerBeanDefinition(beanDefinition);
        }
    }
    
    private void registerDirect(Class<?> ckass)
    {
        BeanDefinition beanDefinition = new BeanDefinition(ckass.getName(), ckass, false);
        beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(ckass));
        environment.registerBeanDefinition(beanDefinition);
    }
    
    public void setClassLoader(ClassLoader classLoader)
    {
        environment.setClassLoader(classLoader);
    }
    
    public void addProperties(Properties properties)
    {
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            environment.putProperty((String) entry.getKey(), (String) entry.getValue());
        }
    }
}
