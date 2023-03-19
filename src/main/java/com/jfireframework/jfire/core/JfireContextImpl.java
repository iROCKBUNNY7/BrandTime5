package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.aop.EnhanceManager;
import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;
import com.jfireframework.jfire.core.beandescriptor.ClassBeanDescriptor;
import com.jfireframework.jfire.core.beanfactory.DefaultClassBeanFactory;
import com.jfireframework.jfire.core.beanfactory.DefaultMethodBeanFactory;
import com.jfireframework.jfire.core.prepare.JfirePrepare;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JfireContextImpl implements JfireContext
{
    protected Map<String, BeanDefinition>                   beanDefinitionMap             = new HashMap<String, BeanDefinition>();
    protected ConcurrentMap<Class<?>, List<BeanDefinition>> classToBeanDefinitionsMap     = new ConcurrentHashMap<Class<?>, List<BeanDefinition>>();
    private   Environment                                   environment;
    private   AnnotationContextFactory                      annotationContextFactory      = new SupportOverrideAttributeAnnotationContextFactory();
    private   Set<BeanDefinition>                           configurationBeanDefinitions  = new HashSet<BeanDefinition>();
    private   Set<BeanDefinition>                           jfirePrepareBeanDefinitions   = new HashSet<BeanDefinition>();
    private   Set<BeanDefinition>                           enhanceManagerBeanDefinitions = new HashSet<BeanDefinition>();
    private   Set<BeanDefinition>                           beanFactoryBeanDefinitions    = new HashSet<BeanDefinition>();

    public JfireContextImpl()
    {
        registerApplicationContext();
        registerDefaultBeanFactory();
        registerAnnotationContextFactory();
        registerDefaultMethodBeanFatory();
    }

    private void registerDefaultMethodBeanFatory()
    {
        BeanDescriptor beanDescriptor = new ClassBeanDescriptor(DefaultMethodBeanFactory.class, "defaultMethodBeanFactory", false, DefaultClassBeanFactory.class);
        BeanDefinition beanDefinition = new BeanDefinition(beanDescriptor);
        beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
    }

    private void registerAnnotationContextFactory()
    {
        BeanDefinition beanDefinition = new BeanDefinition("annotationContextFactory", SupportOverrideAttributeAnnotationContextFactory.class, annotationContextFactory);
        beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
    }

    private void registerDefaultBeanFactory()
    {
        BeanFactory    beanFactory    = new DefaultClassBeanFactory(annotationContextFactory);
        BeanDefinition beanDefinition = new BeanDefinition(DefaultClassBeanFactory.class.getName(), DefaultClassBeanFactory.class, beanFactory);
        beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
    }

    private void registerApplicationContext()
    {
        BeanDefinition beanDefinition = new BeanDefinition("applicationContext", ApplicationContext.class, this);
        registerBeanDefinition(beanDefinition);
    }

    @Override
    public void refresh()
    {
        List<JfirePrepare> jfirePrepares = new ArrayList<JfirePrepare>();
        for (BeanDefinition each : jfirePrepareBeanDefinitions)
        {
            jfirePrepares.add((JfirePrepare) each.getBean());
        }
        Collections.sort(jfirePrepares, new Comparator<JfirePrepare>()
        {
            @Override
            public int compare(JfirePrepare o1, JfirePrepare o2)
            {
                return o1.order() > o2.order() ? 1 : o1.order() == o2.order() ? 0 : -1;
            }
        });
        for (JfirePrepare each : jfirePrepares)
        {
            each.prepare(this);
        }
    }

    @Override
    public int importClass(Class<?> ckass)
    {
        return 0;
    }

    @Override
    public boolean registerBeanDefinition(BeanDefinition beanDefinition)
    {
        return beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition) == null;
    }

    @Override
    public boolean registerConfiguration(Class<?> ckass)
    {
        BeanDescriptor beanDescriptor = new ClassBeanDescriptor(ckass, ckass.getName(), false, DefaultClassBeanFactory.class);
        if (beanDefinitionMap.containsKey(beanDescriptor.beanName()))
        {
            return false;
        }
        BeanDefinition beanDefinition = new BeanDefinition(beanDescriptor);
        beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
        configurationBeanDefinitions.add(beanDefinition);
        return true;
    }

    @Override
    public boolean registerJfirePrepare(Class<? extends JfirePrepare> ckass)
    {
        try
        {
            JfirePrepare   jfirePrepare   = ckass.newInstance();
            BeanDefinition beanDefinition = new BeanDefinition(ckass.getName(), ckass, jfirePrepare);
            if (beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition) == null)
            {
                jfirePrepareBeanDefinitions.add(beanDefinition);
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return false;
        }
    }

    @Override
    public boolean registerEnhanceManager(Class<? extends EnhanceManager> ckass)
    {
        try
        {
            EnhanceManager enhanceManager = ckass.newInstance();
            BeanDefinition beanDefinition = new BeanDefinition(ckass.getName(), ckass, enhanceManager);
            if (beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition) == null)
            {
                enhanceManagerBeanDefinitions.add(beanDefinition);
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return false;
        }
    }

    @Override
    public AnnotationContextFactory getAnnotationContextFactory()
    {
        return annotationContextFactory;
    }

    @Override
    public void init()
    {
    }

    @Override
    public BeanFactory getBeanFactory(BeanDescriptor beanDescriptor)
    {
        if (StringUtil.isNotBlank(beanDescriptor.selectedBeanFactoryBeanName()))
        {
            return getBean(beanDescriptor.selectedBeanFactoryBeanName());
        }
        if (beanDescriptor.selectedBeanFactoryBeanClass() != null)
        {
            return (BeanFactory) getBean(beanDescriptor.selectedBeanFactoryBeanClass());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Environment getEnv()
    {
        return environment;
    }

    @Override
    public <E> E getBean(Class<E> ckass)
    {
        List<BeanDefinition> beanDefinitions = getBeanDefinitions(ckass);
        return (E) beanDefinitions.get(0).getBean();
    }

    private <E> List<BeanDefinition> getBeanDefinitions(Class<E> ckass)
    {
        List<BeanDefinition> beanDefinitions = classToBeanDefinitionsMap.get(ckass);
        if (beanDefinitions == null)
        {
            beanDefinitions = new LinkedList<BeanDefinition>();
            for (BeanDefinition each : beanDefinitionMap.values())
            {
                if (ckass == each.getType() || ckass.isAssignableFrom(each.getType()))
                {
                    beanDefinitions.add(each);
                }
            }
            classToBeanDefinitionsMap.putIfAbsent(ckass, beanDefinitions);
            beanDefinitions = classToBeanDefinitionsMap.get(ckass);
        }
        return beanDefinitions;
    }

    @Override
    public <E> List<E> getBeans(Class<E> ckass)
    {
        List<BeanDefinition> beanDefinitions = getBeanDefinitions(ckass);
        List<E>              list            = new LinkedList<E>();
        for (BeanDefinition each : beanDefinitions)
        {
            list.add((E) each.getBean());
        }
        return list;
    }

    @Override
    public <E> E getBean(String beanName)
    {
        return (E) beanDefinitionMap.get(beanName).getBean();
    }
}
