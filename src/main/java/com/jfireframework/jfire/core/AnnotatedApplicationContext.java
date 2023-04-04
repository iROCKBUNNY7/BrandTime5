package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.compiler.CompileHelper;
import com.jfireframework.jfire.core.aop.EnhanceManager;
import com.jfireframework.jfire.core.aop.impl.AopEnhanceManager;
import com.jfireframework.jfire.core.aop.impl.CacheAopManager;
import com.jfireframework.jfire.core.aop.impl.TransactionAopManager;
import com.jfireframework.jfire.core.aop.impl.ValidateAopManager;
import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;
import com.jfireframework.jfire.core.beandescriptor.ClassBeanDescriptor;
import com.jfireframework.jfire.core.beanfactory.DefaultClassBeanFactory;
import com.jfireframework.jfire.core.beanfactory.DefaultMethodBeanFactory;
import com.jfireframework.jfire.core.beanfactory.SelectBeanFactory;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.Import;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfire.core.prepare.processor.ConfigurationProcessor;
import com.jfireframework.jfire.exception.BeanDefinitionCanNotFindException;

import javax.annotation.Resource;
import javax.tools.JavaCompiler;
import java.util.*;

public class AnnotatedApplicationContext implements JfireContext
{
    protected Map<String, BeanDefinition> beanDefinitionMap          = new HashMap<String, BeanDefinition>();
    private   Environment                 environment                = new Environment.EnvironmentImpl();
    private   AnnotationContextFactory    annotationContextFactory   = new SupportOverrideAttributeAnnotationContextFactory();
    private   Map<String, BeanDefinition> unRemovableBeanDefinition  = new HashMap<String, BeanDefinition>();
    private   JavaCompiler                javaCompiler;
    private   CompileHelper               compileHelper;
    private   boolean                     firstRefresh               = false;
    private   boolean                     configurationClassSetBuild = false;
    private   Set<Class<?>>               configurationClassSet      = new HashSet<Class<?>>();
    private   Class<?>                    bootStarpClass;

    public AnnotatedApplicationContext(Class<?> bootStarpClass)
    {
        this.bootStarpClass = bootStarpClass;
        registerConfiguration(bootStarpClass);
    }

    public AnnotatedApplicationContext(Class<?> bootStarpClass, JavaCompiler javaCompiler)
    {
        this.bootStarpClass = bootStarpClass;
        registerConfiguration(bootStarpClass);
        setJavaCompiler(javaCompiler);
    }

    public AnnotatedApplicationContext()
    {
    }

    public AnnotatedApplicationContext(JavaCompiler javaCompiler)
    {
        setJavaCompiler(javaCompiler);
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

    private void registerJfireContext()
    {
        BeanDefinition beanDefinition = new BeanDefinition("jfireContext", JfireContext.class, this);
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
        beanDefinitionMap.clear();
        registerJfireContext();
        registerDefaultBeanFactory();
        registerAnnotationContextFactory();
        registerDefaultMethodBeanFatory();
        registerBean(AopEnhanceManager.class);
        registerBean(TransactionAopManager.class);
        registerBean(CacheAopManager.class);
        registerBean(ValidateAopManager.class);
        registerJfirePrepare(ConfigurationProcessor.class);
        beanDefinitionMap.putAll(unRemovableBeanDefinition);
        if (processConfigurationImports())
        {
            refresh();
            return;
        }
        if (processJfirePrepare() == NeedRefresh.YES)
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
        List<JfirePrepare> jfirePrepares = new ArrayList<JfirePrepare>();
        jfirePrepares.addAll(getBeans(JfirePrepare.class));
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
            if (each.prepare(this) == NeedRefresh.YES)
            {
                return NeedRefresh.YES;
            }
        }
        return NeedRefresh.NO;
    }

    private boolean processConfigurationImports()
    {
        boolean needRefresh = false;
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
                        RegisterResult registerClass = registerClass(importClass);
                        if (registerClass == RegisterResult.JFIREPREPARE || registerClass == RegisterResult.CONFIGURATION)
                        {
                            needRefresh = NEED_REFRESH;
                        }
                    }
                }
            }
        }
        return needRefresh;
    }

    @Override
    public RegisterResult registerClass(Class<?> ckass)
    {
        if (JfirePrepare.class.isAssignableFrom(ckass))
        {
            return registerJfirePrepare((Class<? extends JfirePrepare>) ckass) ? RegisterResult.JFIREPREPARE : RegisterResult.NODATA;
        }
        else if (annotationContextFactory.get(ckass, Thread.currentThread().getContextClassLoader()).isAnnotationPresent(Configuration.class))
        {
            return registerConfiguration(ckass) ? RegisterResult.CONFIGURATION : RegisterResult.NODATA;
        }
        else
        {
            return registerBean((Class<? extends EnhanceManager>) ckass) ? RegisterResult.BEAN : RegisterResult.NODATA;
        }
    }

    @Override
    public boolean registerBean(Class<?> ckass)
    {
        return registerBean(ckass, false);
    }

    private boolean registerBean(Class<?> ckass, boolean unremoveable)
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
        if (unremoveable && unRemovableBeanDefinition.containsKey(beanName))
        {
            return false;
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
        if (unremoveable)
        {
            unRemovableBeanDefinition.put(beanDefinition.getBeanName(), beanDefinition);
            return true;
        }
        else
        {
            return registerBeanDefinition(beanDefinition);
        }
    }

    @Override
    public boolean registerBeanDefinition(BeanDefinition beanDefinition)
    {
        return beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition) == null;
    }

    private boolean registerConfiguration(Class<?> ckass)
    {
        String beanName = ckass.getName();
        if (unRemovableBeanDefinition.containsKey(beanName))
        {
            return false;
        }
        BeanDescriptor beanDescriptor = new ClassBeanDescriptor(ckass, ckass.getName(), false, DefaultClassBeanFactory.class);
        BeanDefinition beanDefinition = new BeanDefinition(beanDescriptor);
        unRemovableBeanDefinition.put(beanDefinition.getBeanName(), beanDefinition);
        return true;
    }

    private boolean registerJfirePrepare(Class<? extends JfirePrepare> ckass)
    {
        String beanName = ckass.getName();
        if (unRemovableBeanDefinition.containsKey(beanName))
        {
            return false;
        }
        try
        {
            BeanDefinition beanDefinition = new BeanDefinition(beanName, ckass, ckass.newInstance());
            unRemovableBeanDefinition.put(beanName, beanDefinition);
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
        }
        return true;
    }

    @Override
    public void setJavaCompiler(JavaCompiler javaCompiler)
    {
        this.javaCompiler = javaCompiler;
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
        compileHelper = javaCompiler == null ? new CompileHelper() : new CompileHelper(Thread.currentThread().getContextClassLoader(), javaCompiler);
        return compileHelper;
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
            if (JfireAwareContextInited.class.isAssignableFrom(beanDefinition.getType()))
            {
                ((JfireAwareContextInited) beanDefinition.getBean()).awareContextInited(this);
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
    public BeanDefinition getBeanFactory(BeanDescriptor beanDescriptor)
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
    public void register(Class<?> ckass)
    {
        if (JfirePrepare.class.isAssignableFrom(ckass))
        {
            registerJfirePrepare((Class<? extends JfirePrepare>) ckass);
        }
        else
        {
            registerBean((Class<?>) ckass, true);
        }
    }
}
