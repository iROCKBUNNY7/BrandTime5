package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.jfire.core.aop.AopManager;
import com.jfireframework.jfire.core.aop.impl.CacheAopManager;
import com.jfireframework.jfire.core.aop.impl.DefaultAopManager;
import com.jfireframework.jfire.core.aop.impl.TransactionAopManager;
import com.jfireframework.jfire.core.aop.impl.ValidateAopManager;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
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
import com.jfireframework.jfire.util.Utils;

import javax.annotation.Resource;
import javax.tools.JavaCompiler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

public class JfireBootstrap
{
    private Environment environment = new Environment();
    private Set<JfirePrepare> jfirePrepares = new HashSet<JfirePrepare>();
    private Set<AopManager> aopManagers = new HashSet<AopManager>();

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
        registerAopManager(new DefaultAopManager());
        registerAopManager(new TransactionAopManager());
        registerAopManager(new CacheAopManager());
        registerAopManager(new ValidateAopManager());
        /**/
        registerJfirePrepare(new ImportProcessor());
        registerJfirePrepare(new AddPropertyProcessor());
        registerJfirePrepare(new ComponentScanProcessor());
        registerJfirePrepare(new ConfigurationProcessor());
        registerJfirePrepare(new EnableAutoConfigurationProcessor());
        registerJfirePrepare(new ProfileSelectorProcessor());
        registerJfirePrepare(new PropertyPathProcessor());
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
            if ( beanDefinition.isAwareContextInit() )
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
        LinkedList<JfirePrepare> list = new LinkedList<JfirePrepare>(this.jfirePrepares);
        Collections.sort(list, new Comparator<JfirePrepare>()
        {
            @Override
            public int compare(JfirePrepare o1, JfirePrepare o2)
            {
                return o1.order() > o2.order() ? 1 : o1.order() == o2.order() ? 0 : -1;
            }
        });
        for (JfirePrepare jfirePrepare : list)
        {
            jfirePrepare.prepare(environment);
        }
    }

    private void aopScan(Environment environment)
    {
        LinkedList<AopManager> list = new LinkedList<AopManager>(this.aopManagers);
        Collections.sort(list, new Comparator<AopManager>()
        {
            @Override
            public int compare(AopManager o1, AopManager o2)
            {
                return o1.order() > o2.order() ? 1 : o1.order() == o2.order() ? 0 : -1;
            }
        });
        for (AopManager aopManager : list)
        {
            aopManager.scan(environment);
        }
    }

    public void register(BeanDefinition beanDefinition)
    {
        beanDefinition.check();
        environment.registerBeanDefinition(beanDefinition);
    }

    public void register(Class<?> ckass)
    {
        if ( Utils.ANNOTATION_UTIL.isPresent(Resource.class, ckass) )
        {
            Resource resource = Utils.ANNOTATION_UTIL.getAnnotation(Resource.class, ckass);
            String beanName = StringUtil.isNotBlank(resource.name()) ? resource.name() : ckass.getName();
            boolean prototype = !resource.shareable();
            BeanDefinition beanDefinition = new BeanDefinition(beanName, ckass, prototype);
            beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(ckass));
            environment.registerBeanDefinition(beanDefinition);
        }
    }

    public void setClassLoader(ClassLoader classLoader)
    {
        environment.setClassLoader(classLoader);
    }

    public void setJavaCompiler(JavaCompiler javaCompiler)
    {
        environment.setJavaCompiler(javaCompiler);
    }

    public void addProperties(Properties properties)
    {
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            environment.putProperty((String) entry.getKey(), (String) entry.getValue());
        }
    }

    public void registerJfirePrepare(JfirePrepare jfirePrepare)
    {
        jfirePrepares.add(jfirePrepare);
    }

    public void registerAopManager(AopManager aopManager)
    {
        aopManagers.add(aopManager);
    }
}
