package com.jfirer.jfire.core;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.impl.AopEnhanceManager;
import com.jfirer.jfire.core.aop.impl.CacheAopManager;
import com.jfirer.jfire.core.aop.impl.TransactionAopManager;
import com.jfirer.jfire.core.aop.impl.ValidateAopManager;
import com.jfirer.jfire.core.beandescriptor.ClassReflectInstanceDescriptor;
import com.jfirer.jfire.core.beandescriptor.InstanceDescriptor;
import com.jfirer.jfire.core.beandescriptor.SelectedBeanFactoryInstanceDescriptor;
import com.jfirer.jfire.core.beanfactory.DefaultClassBeanFactory;
import com.jfirer.jfire.core.beanfactory.DefaultMethodBeanFactory;
import com.jfirer.jfire.core.beanfactory.SelectBeanFactory;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.Import;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.core.prepare.processor.ConfigurationProcessor;
import com.jfirer.jfire.exception.BeanDefinitionCanNotFindException;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.util.*;

public class DefaultApplicationContext implements ApplicationContext
{
    /**
     * 只有Configuration注解下并且有Conditional注解的情况下，才会有Bean是否被注册的可能。
     * 如果支持每一轮刷新都根据不同的环境变量或者其他条件满足来增减Bean定义会变得较为复杂，而且实际上也没有遇到这样的场景。
     * 因为目前简化为只支持Bean定义不断增多。
     */
    protected Map<String, BeanDefinition> beanDefinitionMap          = new HashMap<String, BeanDefinition>();
    private   Environment                 environment                = new Environment.EnvironmentImpl();
    private   AnnotationContextFactory    annotationContextFactory   = new SupportOverrideAttributeAnnotationContextFactory();
    private   CompileHelper               compileHelper;
    private   boolean                     firstRefresh               = false;
    private   boolean                     configurationClassSetBuild = false;
    private   Set<Class<?>>               configurationClassSet      = new HashSet<Class<?>>();
    private   Class<?>                    bootStarpClass;

    public DefaultApplicationContext(Class<?> bootStarpClass)
    {
        this(bootStarpClass, new CompileHelper());
    }

    public DefaultApplicationContext(Class<?> bootStarpClass, CompileHelper compileHelper)
    {
        this.bootStarpClass = bootStarpClass;
        register(bootStarpClass);
        this.compileHelper = compileHelper;
    }

    public DefaultApplicationContext()
    {
        compileHelper = new CompileHelper();
    }

    public DefaultApplicationContext(CompileHelper compileHelper)
    {
        this.compileHelper = compileHelper;
    }

    private void refreshIfNeed()
    {
        if (firstRefresh == false)
        {
            refresh();
        }
    }

    private void registerDefaultMethodBeanFatory()
    {
        InstanceDescriptor instanceDescriptor = new ClassReflectInstanceDescriptor( DefaultMethodBeanFactory.class);
        BeanDefinition     beanDefinition = new BeanDefinition("defaultMethodBeanFactory", DefaultMethodBeanFactory.class, false,instanceDescriptor);
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
        BeanDefinition beanDefinition = new BeanDefinition("jfireContext", ApplicationContext.class, this);
        registerBeanDefinition(beanDefinition);
    }

    @Override
    public void refresh()
    {
        firstRefresh = true;
        configurationClassSetBuild = false;
        if (TRACEID.currentTraceId() == null)
        {
            TRACEID.newTraceId();
        }
        registerApplicationContext();
        registerDefaultBeanFactory();
        registerAnnotationContextFactory();
        registerDefaultMethodBeanFatory();
        register(AopEnhanceManager.class);
        register(TransactionAopManager.class);
        register(CacheAopManager.class);
        register(ValidateAopManager.class);
        registerJfirePrepare(ConfigurationProcessor.class);
        if (processConfigurationImports() == ApplicationContext.NeedRefresh.YES)
        {
            refresh();
            return;
        }
        if (processJfirePrepare() == ApplicationContext.NeedRefresh.YES)
        {
            refresh();
            return;
        }
        if (beanDefinitionMap.isEmpty())
        {
            return;
        }
        invokeBeanDefinitionInitMethod();
        aopScan();
        awareContextInit();
    }

    private NeedRefresh processJfirePrepare()
    {
        List<ContextPrepare> jfirePrepares = new ArrayList<ContextPrepare>();
        jfirePrepares.addAll(getBeans(ContextPrepare.class));
        Collections.sort(jfirePrepares, new Comparator<ContextPrepare>()
        {
            @Override
            public int compare(ContextPrepare o1, ContextPrepare o2)
            {
                return o1.order() > o2.order() ? 1 : o1.order() == o2.order() ? 0 : -1;
            }
        });
        for (ContextPrepare each : jfirePrepares)
        {
            if (each.prepare(this) == ApplicationContext.NeedRefresh.YES)
            {
                return ApplicationContext.NeedRefresh.YES;
            }
        }
        return ApplicationContext.NeedRefresh.NO;
    }

    private NeedRefresh processConfigurationImports()
    {
        NeedRefresh needRefresh = ApplicationContext.NeedRefresh.NO;
        for (Class<?> each : getConfigurationClassSet())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each);
            if (annotationContext.isAnnotationPresent(Import.class))
            {
                List<Import> imports = annotationContext.getAnnotations(Import.class);
                for (Import anImport : imports)
                {
                    for (Class<?> importClass : anImport.value())
                    {
                        RegisterResult registerClass = register(importClass);
                        if (registerClass == ApplicationContext.RegisterResult.PREPARE || registerClass == ApplicationContext.RegisterResult.CONFIGURATION)
                        {
                            needRefresh = ApplicationContext.NeedRefresh.YES;
                        }
                    }
                }
            }
        }
        return needRefresh;
    }

    @Override
    public RegisterResult register(Class<?> ckass)
    {
        if (ContextPrepare.class.isAssignableFrom(ckass))
        {
            return registerJfirePrepare((Class<? extends ContextPrepare>) ckass) ? ApplicationContext.RegisterResult.PREPARE : ApplicationContext.RegisterResult.NODATA;
        }
        else if (annotationContextFactory.get(ckass).isAnnotationPresent(Configuration.class))
        {
            return registerBean(ckass) ? ApplicationContext.RegisterResult.CONFIGURATION : ApplicationContext.RegisterResult.NODATA;
        }
        else
        {
            return registerBean(ckass) ? ApplicationContext.RegisterResult.BEAN : ApplicationContext.RegisterResult.NODATA;
        }
    }

    private boolean registerBean(Class<?> ckass)
    {
        AnnotationContext annotationContext = annotationContextFactory.get(ckass);
        String            beanName;
        boolean           prototype;
        if (annotationContext.isAnnotationPresent(Resource.class))
        {
            Resource resource = annotationContext.getAnnotation(Resource.class);
            beanName = StringUtil.isNotBlank(resource.name()) ? resource.name() : ckass.getName();
            prototype = resource.shareable() == false;
        }
        else
        {
            beanName = ckass.getName();
            prototype = false;
        }
        if (beanDefinitionMap.containsKey(beanName))
        {
            return false;
        }
        InstanceDescriptor instanceDescriptor;
        if (annotationContext.isAnnotationPresent(SelectBeanFactory.class))
        {
            SelectBeanFactory selectBeanFactory = annotationContext.getAnnotation(SelectBeanFactory.class);
            if (StringUtil.isNotBlank(selectBeanFactory.value()))
            {
                instanceDescriptor = new SelectedBeanFactoryInstanceDescriptor(selectBeanFactory.value(),ckass);
            }
            else if (selectBeanFactory.beanFactoryType() != Object.class)
            {
                instanceDescriptor = new SelectedBeanFactoryInstanceDescriptor(selectBeanFactory.beanFactoryType(),ckass);
            }
            else
            {
                throw new IllegalArgumentException("类:" + ckass.getName() + "上的注解：SelectBeanFactory缺少正确的属性值");
            }
        }
        else
        {
            instanceDescriptor = new ClassReflectInstanceDescriptor(ckass);
        }
        BeanDefinition beanDefinition = new BeanDefinition(beanName,ckass,prototype,instanceDescriptor);
        beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
        return true;
    }

    @Override
    public boolean registerBeanDefinition(BeanDefinition beanDefinition)
    {
        return beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition) == null;
    }

    private boolean registerJfirePrepare(Class<? extends ContextPrepare> ckass)
    {
        String beanName = ckass.getName();
        if (beanDefinitionMap.containsKey(beanName))
        {
            return false;
        }
        try
        {
            BeanDefinition beanDefinition = new BeanDefinition(beanName, ckass, ckass.newInstance());
            beanDefinitionMap.put(beanName, beanDefinition);
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
        }
        return true;
    }

    @Override
    public Set<Class<?>> getConfigurationClassSet()
    {
        if (configurationClassSetBuild == false)
        {
            configurationClassSet.clear();
            for (BeanDefinition value : beanDefinitionMap.values())
            {
                if (annotationContextFactory.get(value.getType()).isAnnotationPresent(Configuration.class))
                {
                    configurationClassSet.add(value.getType());
                }
            }
            if (bootStarpClass != null)
            {
                configurationClassSet.add(bootStarpClass);
            }
            configurationClassSetBuild = true;
        }
        return configurationClassSet;
    }

    @Override
    public CompileHelper getCompileHelper()
    {
        if (compileHelper != null)
        {
            return compileHelper;
        }
        else
        {
            compileHelper = new CompileHelper();
            return compileHelper;
        }
    }

    @Override
    public AnnotationContextFactory getAnnotationContextFactory()
    {
        return annotationContextFactory;
    }

    private void awareContextInit()
    {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values())
        {
            if (AwareContextInited.class.isAssignableFrom(beanDefinition.getType()))
            {
                ((AwareContextInited) beanDefinition.getBean()).aware(this);
            }
        }
    }

    private void invokeBeanDefinitionInitMethod()
    {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values())
        {
            beanDefinition.init(this);
        }
    }

    private void aopScan()
    {
        LinkedList<EnhanceManager> list = new LinkedList<EnhanceManager>();
        list.addAll(getBeans(EnhanceManager.class));
        Collections.sort(list, new Comparator<EnhanceManager>()
        {
            @Override
            public int compare(EnhanceManager o1, EnhanceManager o2)
            {
                return o1.order() > o2.order() ? 1 : o1.order() == o2.order() ? 0 : -1;
            }
        });
        for (EnhanceManager aopManager : list)
        {
            aopManager.scan(this);
        }
        for (BeanDefinition each : beanDefinitionMap.values())
        {
            each.initEnhance();
        }
    }

    @Override
    public Collection<BeanDefinition> getAllBeanDefinitions()
    {
        return beanDefinitionMap.values();
    }

    @Override
    public BeanDefinition getBeanDefinition(Class<?> ckass)
    {
        for (BeanDefinition each : beanDefinitionMap.values())
        {
            if (ckass == each.getType() || ckass.isAssignableFrom(each.getType()))
            {
                return each;
            }
        }
        return null;
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName)
    {
        return beanDefinitionMap.get(beanName);
    }

    @Override
    public BeanDefinition getBeanFactory(InstanceDescriptor beanDescriptor)
    {
        if (StringUtil.isNotBlank(beanDescriptor.selectedBeanFactoryBeanName()))
        {
            return getBeanDefinition(beanDescriptor.selectedBeanFactoryBeanName());
        }
        if (beanDescriptor.selectedBeanFactoryBeanClass() != null)
        {
            return getBeanDefinition(beanDescriptor.selectedBeanFactoryBeanClass());
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
        refreshIfNeed();
        BeanDefinition beanDefinition = getBeanDefinition(ckass);
        if (beanDefinition == null)
        {
            throw new BeanDefinitionCanNotFindException(ckass);
        }
        return (E) beanDefinition.getBean();
    }

    public List<BeanDefinition> getBeanDefinitions(Class<?> ckass)
    {
        List<BeanDefinition> beanDefinitions = new LinkedList<BeanDefinition>();
        for (BeanDefinition each : beanDefinitionMap.values())
        {
            if (ckass == each.getType() || ckass.isAssignableFrom(each.getType()))
            {
                beanDefinitions.add(each);
            }
        }
        return beanDefinitions;
    }

    @Override
    public <E> List<E> getBeans(Class<E> ckass)
    {
        refreshIfNeed();
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
        refreshIfNeed();
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null)
        {
            throw new BeanDefinitionCanNotFindException(beanName);
        }
        return (E) beanDefinition.getBean();
    }

    @Override
    public List<BeanDefinition> getBeanDefinitionsByAnnotation(Class<? extends Annotation> ckass)
    {
        List<BeanDefinition> list = new ArrayList<BeanDefinition>();
        for (BeanDefinition each : beanDefinitionMap.values())
        {
            Class<?>          type              = each.getType();
            AnnotationContext annotationContext = annotationContextFactory.get(type);
            if (annotationContext.isAnnotationPresent(ckass))
            {
                list.add(each);
            }
        }
        return list;
    }
}
