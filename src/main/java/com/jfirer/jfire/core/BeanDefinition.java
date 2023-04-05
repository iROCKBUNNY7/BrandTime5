package com.jfirer.jfire.core;

import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import com.jfirer.jfire.core.aop.EnhanceCallbackForBeanInstance;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.ProceedPoint;
import com.jfirer.jfire.core.aop.ProceedPointImpl;
import com.jfirer.jfire.core.beandescriptor.BeanDescriptor;
import com.jfirer.jfire.core.inject.InjectHandler;
import com.jfirer.jfire.core.inject.impl.DefaultDependencyInjectHandler;
import com.jfirer.jfire.core.inject.impl.DefaultPropertyInjectHandler;
import com.jfirer.jfire.core.inject.notated.PropertyRead;
import com.jfirer.jfire.exception.EnhanceException;
import com.jfirer.jfire.exception.NewBeanInstanceException;
import com.jfirer.jfire.exception.PostConstructMethodException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class BeanDefinition
{
    public static final ThreadLocal<Map<String, Object>> tmpBeanInstanceMap = new ThreadLocal<Map<String, Object>>()
    {
        @Override
        protected java.util.Map<String, Object> initialValue()
        {
            return new HashMap<String, Object>();
        }
    };
    private             boolean                          prototype;
    private             boolean                          awareContextInit;
    // 如果是单例的情况，后续只会使用该实例
    private volatile    Object                           cachedSingtonInstance;
    /******/
    // 该Bean的类
    private             Class<?>                         type;
    // 增强后的类，如果没有增强标记，该属性为空
    private             Class<?>                         enhanceType;
    private             Set<EnhanceManager>              aopManagers        = new HashSet<EnhanceManager>();
    private             EnhanceManager[]                 orderedAopManagers;
    private             EnhanceCallbackForBeanInstance[] enhanceCallbackForBeanInstances;
    private             String                           beanName;
    // 标注@PostConstruct的方法
    private             Method                           postConstructMethod;
    private             InjectHandler[]                  injectHandlers;
    private             JfireContext                     jfireContext;
    private             BeanDescriptor                   beanDescriptor;
    private             BeanFactory                      beanFactory;
    private             BeanDefinition                   beanFactoryBeanDefinition;

    public BeanDefinition(BeanDescriptor beanDescriptor)
    {
        this.beanDescriptor = beanDescriptor;
        this.beanName = beanDescriptor.beanName();
        if (beanDescriptor.getDescriptorClass() != null)
        {
            type = beanDescriptor.getDescriptorClass();
        }
        else
        {
            type = beanDescriptor.getDescriptorMethod().getReturnType();
        }
        setPrototype(beanDescriptor.isPrototype());
        if (JfireAwareContextInited.class.isAssignableFrom(type))
        {
            setAwareContextInit(true);
        }
    }

    public BeanDefinition(String beanName, Class<?> type, Object beanInstance)
    {
        this.beanName = beanName;
        this.type = type;
        cachedSingtonInstance = beanInstance;
        setPrototype(false);
        setAwareContextInit(false);
    }

    public void init(JfireContext jfireContext)
    {
        this.jfireContext = jfireContext;
        if (cachedSingtonInstance == null)
        {
            beanFactoryBeanDefinition = jfireContext.getBeanFactory(beanDescriptor);
            initPostConstructMethod();
            initInjectHandlers();
            initAwareContextInit();
        }
    }

    private void initAwareContextInit()
    {
        if (JfireAwareContextInited.class.isAssignableFrom(type))
        {
            setAwareContextInit(true);
        }
    }

    public void initEnhance()
    {
        if (aopManagers.size() == 0 || cachedSingtonInstance != null)
        {
            orderedAopManagers = new EnhanceManager[0];
            return;
        }
        orderedAopManagers = aopManagers.toArray(new EnhanceManager[aopManagers.size()]);
        enhanceCallbackForBeanInstances = new EnhanceCallbackForBeanInstance[orderedAopManagers.length];
        Arrays.sort(orderedAopManagers, new Comparator<EnhanceManager>()
        {

            @Override
            public int compare(EnhanceManager o1, EnhanceManager o2)
            {
                return o1.order() - o2.order();
            }
        });
        ClassModel classModel = new ClassModel(type.getSimpleName() + "$AOP$" + EnhanceManager.classNameCounter.getAndIncrement());
        if (type.isInterface())
        {
            classModel.addInterface(type);
        }
        else
        {
            classModel.setParentClass(type);
        }
        classModel.addImport(ProceedPointImpl.class);
        classModel.addImport(ProceedPoint.class);
        classModel.addImport(Object.class);
        String hostFieldName = "host_" + EnhanceManager.fieldNameCounter.getAndIncrement();
        addHostField(classModel, hostFieldName);
        addSetAopHostMethod(classModel, hostFieldName);
        addInvokeHostPublicMethod(classModel, hostFieldName);
        for (int i = 0; i < orderedAopManagers.length; i++)
        {
            enhanceCallbackForBeanInstances[i] = orderedAopManagers[i].enhance(classModel, type, jfireContext, hostFieldName);
        }
        CompileHelper compiler = jfireContext.getCompileHelper();
        try
        {
            enhanceType = compiler.compile(classModel);
        }
        catch (Throwable e)
        {
            throw new EnhanceException(e);
        }
    }

    private void addInvokeHostPublicMethod(ClassModel classModel, String hostFieldName)
    {
        for (Method each : type.getMethods())
        {
            if (Modifier.isFinal(each.getModifiers()))
            {
                continue;
            }
            MethodModel   methodModel = new MethodModel(each, classModel);
            StringBuilder cache       = new StringBuilder();
            if (each.getReturnType() != void.class)
            {
                cache.append("return ");
            }
            cache.append(hostFieldName).append(".").append(each.getName()).append("(");
            boolean hasComma = false;
            for (int i = 0; i < methodModel.getParamterTypes().length; i++)
            {
                cache.append("$").append(i).append(',');
                hasComma = true;
            }
            if (hasComma)
            {
                cache.setLength(cache.length() - 1);
            }
            cache.append(");");
            methodModel.setBody(cache.toString());
            classModel.putMethodModel(methodModel);
        }
    }

    private void addSetAopHostMethod(ClassModel classModel, String hostFieldName)
    {
        MethodModel setAopHost = new MethodModel(classModel);
        setAopHost.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        setAopHost.setMethodName("setAopHost");
        setAopHost.setParamterTypes(Object.class);
        setAopHost.setReturnType(void.class);
        setAopHost.setBody(hostFieldName + " = (" + SmcHelper.getReferenceName(type, classModel) + ")$0;");
        classModel.putMethodModel(setAopHost);
    }

    private void addHostField(ClassModel classModel, String hostFieldName)
    {
        FieldModel hostField = new FieldModel(hostFieldName, type, classModel);
        classModel.addField(hostField);
        classModel.addInterface(EnhanceManager.SetHost.class);
    }

    /**
     * 初始化bean实例的初始化方法
     */
    private void initPostConstructMethod()
    {
        if (type.isInterface() == false)
        {
            Class<?>                 type                     = this.type;
            boolean                  find                     = false;
            AnnotationContextFactory annotationContextFactory = jfireContext.getAnnotationContextFactory();
            while (type != Object.class)
            {
                for (Method each : type.getDeclaredMethods())
                {
                    if (each.getParameterTypes().length == 0)
                    {
                        if (annotationContextFactory.get(each, Thread.currentThread().getContextClassLoader()).isAnnotationPresent(PostConstruct.class))
                        {
                            postConstructMethod = each;
                            postConstructMethod.setAccessible(true);
                            find = true;
                            break;
                        }
                    }
                }
                if (find)
                {
                    break;
                }
                else
                {
                    type = type.getSuperclass();
                }
            }
        }
    }

    /**
     * 初始化属性注入处理器
     */
    private void initInjectHandlers()
    {
        if (type.isInterface() == false)
        {
            List<InjectHandler> list = new LinkedList<InjectHandler>();
            for (Field each : getAllFields(type))
            {
                if (each.isAnnotationPresent(InjectHandler.CustomInjectHanlder.class))
                {
                    InjectHandler.CustomInjectHanlder customDiHanlder = each.getAnnotation(InjectHandler.CustomInjectHanlder.class);
                    try
                    {
                        InjectHandler newInstance = customDiHanlder.value().newInstance();
                        newInstance.init(each, jfireContext);
                        list.add(newInstance);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                else if (each.isAnnotationPresent(Resource.class))
                {
                    DefaultDependencyInjectHandler injectHandler = new DefaultDependencyInjectHandler();
                    injectHandler.init(each, jfireContext);
                    list.add(injectHandler);
                }
                else if (each.isAnnotationPresent(PropertyRead.class))
                {
                    DefaultPropertyInjectHandler injectHandler = new DefaultPropertyInjectHandler();
                    injectHandler.init(each, jfireContext);
                    list.add(injectHandler);
                }
            }
            injectHandlers = list.toArray(new InjectHandler[list.size()]);
        }
        else
        {
            injectHandlers = new InjectHandler[0];
        }
    }

    public BeanDefinition setBeanName(String beanName)
    {
        this.beanName = beanName;
        return this;
    }

    public BeanDefinition setType(Class<?> type)
    {
        this.type = type;
        return this;
    }

    /**
     * 返回Bean的实例
     *
     * @return
     */
    public Object getBean()
    {
        if (isPropertype())
        {
            return buildInstance();
        }
        else if (cachedSingtonInstance != null)
        {
            return cachedSingtonInstance;
        }
        else
        {
            synchronized (this)
            {
                if (cachedSingtonInstance != null)
                {
                    return cachedSingtonInstance;
                }
                cachedSingtonInstance = buildInstance();
                return cachedSingtonInstance;
            }
        }
    }

    private boolean isPropertype()
    {
        return prototype;
    }

    private synchronized Object buildInstance()
    {
        if (beanFactory == null)
        {
            beanFactory = (BeanFactory) beanFactoryBeanDefinition.getBean();
        }
        Map<String, Object> map       = tmpBeanInstanceMap.get();
        boolean             cleanMark = map.isEmpty();
        Object              instance  = map.get(beanName);
        if (instance != null)
        {
            return instance;
        }
        Object originInstance;
        originInstance = instance = beanFactory.getBean(beanDescriptor);
        if (orderedAopManagers != null && orderedAopManagers.length != 0)
        {
            try
            {
                EnhanceManager.SetHost newInstance = (EnhanceManager.SetHost) enhanceType.newInstance();
                newInstance.setAopHost(originInstance);
                instance = newInstance;
                for (EnhanceCallbackForBeanInstance each : enhanceCallbackForBeanInstances)
                {
                    each.run(instance);
                }
            }
            catch (Throwable e)
            {
                throw new NewBeanInstanceException(e);
            }
        }
        map.put(beanName, instance);
        if (injectHandlers.length != 0)
        {
            for (InjectHandler each : injectHandlers)
            {
                each.inject(originInstance);
            }
        }
        if (postConstructMethod != null)
        {
            try
            {
                postConstructMethod.invoke(instance);
            }
            catch (Exception e)
            {
                throw new PostConstructMethodException(e);
            }
        }
        if (cleanMark)
        {
            map.clear();
        }
        return instance;
    }

    /**
     * 获取该类的所有field对象，如果子类重写了父类的field，则只包含子类的field
     *
     * @param entityClass
     * @return
     */
    Field[] getAllFields(Class<?> entityClass)
    {
        Set<Field> set = new TreeSet<Field>(new Comparator<Field>()
        {
            // 只需要去重，并且希望父类的field在返回数组中排在后面，所以比较全部返回1
            @Override
            public int compare(Field o1, Field o2)
            {
                if (o1.getName().equals(o2.getName()))
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
        });
        while (entityClass != Object.class && entityClass != null)
        {
            for (Field each : entityClass.getDeclaredFields())
            {
                set.add(each);
            }
            entityClass = entityClass.getSuperclass();
        }
        return set.toArray(new Field[set.size()]);
    }

    public void setPrototype(boolean prototype)
    {
        this.prototype = prototype;
    }

    private void setAwareContextInit(boolean awareContextInit)
    {
        this.awareContextInit = awareContextInit;
    }

    public String getBeanName()
    {
        return beanName;
    }

    public Class<?> getType()
    {
        return type;
    }

    public void addAopManager(EnhanceManager aopManager)
    {
        aopManagers.add(aopManager);
    }
}