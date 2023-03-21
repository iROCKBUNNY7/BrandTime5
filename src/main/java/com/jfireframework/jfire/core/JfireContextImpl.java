package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.aop.EnhanceManager;
import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;
import com.jfireframework.jfire.core.beandescriptor.ClassBeanDescriptor;
import com.jfireframework.jfire.core.beanfactory.DefaultClassBeanFactory;
import com.jfireframework.jfire.core.beanfactory.DefaultMethodBeanFactory;
import com.jfireframework.jfire.core.beanfactory.SelectBeanFactory;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.Import;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfireel.template.parser.impl.ElseParser;

import javax.annotation.Resource;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JfireContextImpl implements JfireContext
{
    protected Map<String, BeanDefinition>                   beanDefinitionMap         = new HashMap<String, BeanDefinition>();
    protected ConcurrentMap<Class<?>, List<BeanDefinition>> classToBeanDefinitionsMap = new ConcurrentHashMap<Class<?>, List<BeanDefinition>>();
    private   Environment                                   environment;
    private   AnnotationContextFactory                      annotationContextFactory  = new SupportOverrideAttributeAnnotationContextFactory();
    private   Set<Class<?>>                                 configurationClassSet;
    private   Set<Class<?>>                                 jfirePrepareClassSet;
    private   Set<Class<?>>                                 enhanceManagerClassSet;

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
        beanDefinitionMap.clear();
        registerApplicationContext();
        registerDefaultBeanFactory();
        registerAnnotationContextFactory();
        registerDefaultMethodBeanFatory();
        boolean hasNewJfirePrepareOrConfigurationClass = false;
        for (Class<?> each : configurationClassSet)
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each, Thread.currentThread().getContextClassLoader());
            if (annotationContext.isAnnotationPresent(Import.class))
            {
                List<Import> imports = annotationContext.getAnnotations(Import.class);
                for (Import anImport : imports)
                {
                    for (Class<?> importClass : anImport.value())
                    {
                        int registerClass = registerClass(importClass);
                        if (registerClass == 1 || registerClass == 2)
                        {
                            hasNewJfirePrepareOrConfigurationClass = true;
                        }
                    }
                }
            }
        }
        if (hasNewJfirePrepareOrConfigurationClass)
        {
            refresh();
            return;
        }
        List<JfirePrepare> jfirePrepares = new ArrayList<JfirePrepare>();
        for (Class<?> each : jfirePrepareClassSet)
        {
            try
            {
                jfirePrepares.add((JfirePrepare) each.newInstance());
            }
            catch (Throwable e)
            {
                ReflectUtil.throwException(e);
            }
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
            if (each.prepare(this) == false)
            {
                break;
            }
        }
    }

    @Override
    public int registerClass(Class<?> ckass)
    {
        if (JfirePrepare.class.isAssignableFrom(ckass))
        {
            return registerJfirePrepare((Class<? extends JfirePrepare>) ckass) ? 1 : -1;
        }
        else if (annotationContextFactory.get(ckass, Thread.currentThread().getContextClassLoader()).isAnnotationPresent(Configuration.class))
        {
            return registerConfiguration(ckass) ? 2 : -1;
        }
        else if (EnhanceManager.class.isAssignableFrom(ckass))
        {
            return registerEnhanceManager((Class<? extends EnhanceManager>) ckass) ? 3 : -1;
        }
        else
        {
            return registerBean(ckass) ? 4 : -1;
        }
    }

    @Override
    public boolean registerBean(Class<?> ckass)
    {
        AnnotationContext annotationContext = annotationContextFactory.get(ckass, Thread.currentThread().getContextClassLoader());
        String            beanName;
        boolean           prototype;
        if (annotationContext.isAnnotationPresent(Resource.class))
        {
            Resource resource = annotationContext.getAnnotation(Resource.class);
            beanName = StringUtil.isNotBlank(resource.name()) ? resource.name() : ckass.getName();
            prototype = resource.shareable();
        }
        else
        {
            beanName = ckass.getName();
            prototype = false;
        }
        BeanDescriptor beanDescriptor;
        if (annotationContext.isAnnotationPresent(SelectBeanFactory.class))
        {
            SelectBeanFactory selectBeanFactory = annotationContext.getAnnotation(SelectBeanFactory.class);
            if (StringUtil.isNotBlank(selectBeanFactory.value()))
            {
                beanDescriptor = new ClassBeanDescriptor(ckass, beanName, prototype, selectBeanFactory.value());
            }
            else if (selectBeanFactory.beanFactoryType() != Object.class)
            {
                beanDescriptor = new ClassBeanDescriptor(ckass, beanName, prototype, selectBeanFactory.beanFactoryType());
            }
            else
            {
                throw new IllegalArgumentException("类:" + ckass.getName() + "上的注解：SelectBeanFactory缺少正确的属性值");
            }
        }
        else
        {
            beanDescriptor = new ClassBeanDescriptor(ckass, beanName, prototype, DefaultClassBeanFactory.class);
        }
        BeanDefinition beanDefinition = new BeanDefinition(beanDescriptor);
        return registerBeanDefinition(beanDefinition);
    }

    @Override
    public boolean registerBeanDefinition(BeanDefinition beanDefinition)
    {
        return beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition) == null;
    }

    @Override
    public boolean registerConfiguration(Class<?> ckass)
    {
        return configurationClassSet.add(ckass);
    }

    @Override
    public boolean registerJfirePrepare(Class<? extends JfirePrepare> ckass)
    {
        return jfirePrepareClassSet.add(ckass);
    }

    @Override
    public boolean registerEnhanceManager(Class<? extends EnhanceManager> ckass)
    {
        return enhanceManagerClassSet.add(ckass);
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
    public List<BeanDefinition> getAllBeanDefinitions()
    {
        return null;
    }

    @Override
    public BeanDefinition getBeanDefinition(Class<?> ckass)
    {
        return null;
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

    public List<BeanDefinition> getBeanDefinitions(Class<?> ckass)
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
