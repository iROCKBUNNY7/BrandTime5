package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.aop.AopManager;
import com.jfireframework.jfire.core.aop.impl.CacheAopManager;
import com.jfireframework.jfire.core.aop.impl.DefaultAopManager;
import com.jfireframework.jfire.core.aop.impl.TransactionAopManager;
import com.jfireframework.jfire.core.aop.impl.ValidateAopManager;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.Import;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfire.core.prepare.processor.ConfigurationProcessor;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.DefaultBeanInstanceResolver;
import com.jfireframework.jfire.core.resolver.impl.OutterObjectBeanInstanceResolver;
import com.jfireframework.jfire.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.tools.JavaCompiler;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.Map.Entry;

public class JfireBootstrap
{
    private              Environment       environment;
    private              Set<JfirePrepare> jfirePrepares = new HashSet<JfirePrepare>();
    private              Set<AopManager>   aopManagers   = new HashSet<AopManager>();
    private static final Logger            logger        = LoggerFactory.getLogger(JfireBootstrap.class);

    public JfireBootstrap()
    {
        this(null, null, null);
    }

    public JfireBootstrap(Class<?> bootStrapClass)
    {
        this(bootStrapClass, null, null);
    }

    public JfireBootstrap(Class<?> bootStrapClass, ClassLoader classLoader)
    {
        this(bootStrapClass, classLoader, null);
    }

    public JfireBootstrap(Class<?> bootStrapClass, ClassLoader classLoader, JavaCompiler compiler)
    {
        if (classLoader != null)
        {
            environment = new Environment(classLoader);
        }
        else
        {
            environment = new Environment();
        }
        environment.setAnnotationStore(bootStrapClass == null ? new Annotation[0] : bootStrapClass.getAnnotations());
        if (bootStrapClass != null && Utils.ANNOTATION_UTIL.isPresent(Configuration.class, bootStrapClass))
        {
            environment.registerCandidateConfiguration(bootStrapClass.getName());
        }
        if (compiler != null)
        {
            environment.setJavaCompiler(compiler);
        }
    }

    public Jfire start()
    {
        TRACEID.newTraceId();
        Jfire jfire = registerJfireInstance();
        registerDefault();
        processImport();
        prepare(environment);
        aopScan(environment);
        invokeBeanDefinitionInitMethod(environment);
        awareContextInit(environment);
        return jfire;
    }

    private void processImport()
    {
        String traceId = TRACEID.currentTraceId();
        for (Import annotation : environment.getAnnotations(Import.class))
        {
            for (Class<?> each : annotation.value())
            {
                if (JfirePrepare.class.isAssignableFrom(each))
                {
                    try
                    {
                        logger.debug("traceId:{} 导入一个预处理器:{}", traceId, each.getName());
                        addJfirePrepare((JfirePrepare) each.newInstance());
                    }
                    catch (Exception e)
                    {
                        ReflectUtil.throwException(e);
                    }
                }
                else if (Utils.ANNOTATION_UTIL.isPresent(Configuration.class, each))
                {
                    logger.debug("traceId:{} 发现一个候选配置类:{}", traceId, each.getName());
                    environment.registerCandidateConfiguration(each.getName());
                }
                else if (Utils.ANNOTATION_UTIL.isPresent(Resource.class, each))
                {
                    logger.debug("traceId:{} 发现一个Bean:{}", traceId, each.getName());
                    register(each);
                }
            }
        }
    }

    private void registerDefault()
    {
        addAopManager(new DefaultAopManager());
        addAopManager(new TransactionAopManager());
        addAopManager(new CacheAopManager());
        addAopManager(new ValidateAopManager());
        addJfirePrepare(new ConfigurationProcessor());
    }

    private Jfire registerJfireInstance()
    {
        Jfire                jfire          = new Jfire(environment);
        BeanDefinition       beanDefinition = new BeanDefinition(Jfire.class.getName(), Jfire.class, false);
        BeanInstanceResolver resolver       = new OutterObjectBeanInstanceResolver(jfire);
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
        if (Utils.ANNOTATION_UTIL.isPresent(Resource.class, ckass))
        {
            Resource       resource       = Utils.ANNOTATION_UTIL.getAnnotation(Resource.class, ckass);
            String         beanName       = StringUtil.isNotBlank(resource.name()) ? resource.name() : ckass.getName();
            boolean        prototype      = !resource.shareable();
            BeanDefinition beanDefinition = new BeanDefinition(beanName, ckass, prototype);
            beanDefinition.setBeanInstanceResolver(new DefaultBeanInstanceResolver(ckass));
            environment.registerBeanDefinition(beanDefinition);
        }
    }

    public void addProperties(Properties properties)
    {
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            environment.putProperty((String) entry.getKey(), (String) entry.getValue());
        }
    }

    private void addJfirePrepare(JfirePrepare jfirePrepare)
    {
        jfirePrepares.add(jfirePrepare);
    }

    private void addAopManager(AopManager aopManager)
    {
        aopManagers.add(aopManager);
    }
}
