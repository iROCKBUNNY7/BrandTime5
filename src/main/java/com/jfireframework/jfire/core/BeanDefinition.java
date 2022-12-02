package com.jfireframework.jfire.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.core.AopManager.SetHost;
import com.jfireframework.jfire.core.inject.InjectHandler;
import com.jfireframework.jfire.core.inject.InjectHandler.CustomInjectHanlder;
import com.jfireframework.jfire.core.inject.impl.DefaultDependencyInjectHandler;
import com.jfireframework.jfire.core.inject.impl.DefaultPropertyInjectHandler;
import com.jfireframework.jfire.exception.EnhanceException;
import com.jfireframework.jfire.exception.NewBeanInstanceException;
import com.jfireframework.jfire.exception.PostConstructMethodException;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.field.PropertyRead;

public class BeanDefinition
{
    // 多实例标记
    private static final int                             PROTOTYPE          = 1 << 0;
    // 增强标记
    private static final int                             NEED_ENHANCE       = 1 << 1;
    // 持有@PostConstruct注解标记
    private static final int                             POST_CONSTRUCT     = 1 << 2;
    // 如果是单例的情况，后续只会使用该实例
    private volatile Object                              cachedSingtonInstance;
    /******/
    // 该Bean配置的状态
    private int                                          state              = 0;
    // 该Bean的类
    private Class<?>                                     type;
    // 增强后的类，如果没有增强标记，该属性为空
    private Class<?>                                     enhanceType;
    private List<AopManager>                             aopManagers        = new LinkedList<AopManager>();
    private AopManager[]                                 orderedAopManagers;
    private String                                       beanName;
    // 标注@PostConstruct的方法
    private Method                                       postConstructMethod;
    private BeanInstanceResolver                         resolver;
    private InjectHandler[]                              injectHandlers;
    public static final ThreadLocal<Map<String, Object>> tmpBeanInstanceMap = new ThreadLocal<Map<String, Object>>() {
                                                                                @Override
                                                                                protected java.util.Map<String, Object> initialValue()
                                                                                {
                                                                                    return new HashMap<String, Object>();
                                                                                };
                                                                            };
    
    public void init(Environment environment)
    {
        initPostConstructMethod();
        initInjectHandlers(environment);
        initEnvironmentForResolver(environment);
        initEnhance(environment);
    }
    
    private void initEnhance(Environment environment)
    {
        if (isNeedEnhance())
        {
            Collections.sort(aopManagers, new Comparator<AopManager>() {
                
                @Override
                public int compare(AopManager o1, AopManager o2)
                {
                    return o1.order() - o2.order();
                }
            });
            orderedAopManagers = aopManagers.toArray(new AopManager[aopManagers.size()]);
            CompilerModel compilerModel = new CompilerModel(type.getName() + "$AOP$" + AopManager.classNameCounter.getAndIncrement(), type);
            compilerModel.addInterface(SetHost.class);
            for (AopManager aopManager : orderedAopManagers)
            {
                aopManager.enhance(compilerModel, environment);
            }
            JavaStringCompiler compiler = new JavaStringCompiler(environment.getClassLoader());
            try
            {
                enhanceType = compiler.compile(compilerModel);
            }
            catch (Throwable e)
            {
                throw new EnhanceException(e);
            }
        }
    }
    
    class demo$Aop$1
    {
        
    }
    
    /**
     * 初始化bean实例的初始化方法
     */
    private void initPostConstructMethod()
    {
        if (type.isInterface() == false)
        {
            Class<?> type = this.type;
            boolean find = false;
            while (type != Object.class)
            {
                for (Method each : type.getDeclaredMethods())
                {
                    if (each.getParameterTypes().length == 0)
                    {
                        if (Utils.ANNOTATION_UTIL.isPresent(PostConstruct.class, each))
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
     * 
     * @param environment
     */
    private void initInjectHandlers(Environment environment)
    {
        if (type.isInterface() == false)
        {
            List<InjectHandler> list = new LinkedList<InjectHandler>();
            AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
            for (Field each : ReflectUtil.getAllFields(type))
            {
                if (annotationUtil.isPresent(CustomInjectHanlder.class, each))
                {
                    CustomInjectHanlder customDiHanlder = annotationUtil.getAnnotation(CustomInjectHanlder.class, each);
                    try
                    {
                        InjectHandler newInstance = customDiHanlder.value().newInstance();
                        newInstance.init(each, environment);
                        list.add(newInstance);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                else if (annotationUtil.isPresent(Resource.class, each))
                {
                    DefaultDependencyInjectHandler injectHandler = new DefaultDependencyInjectHandler();
                    injectHandler.init(each, environment);
                    list.add(injectHandler);
                }
                else if (annotationUtil.isPresent(PropertyRead.class, each))
                {
                    DefaultPropertyInjectHandler injectHandler = new DefaultPropertyInjectHandler();
                    injectHandler.init(each, environment);
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
    
    private void initEnvironmentForResolver(Environment environment)
    {
        resolver.init(environment);
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
    
    public BeanDefinition setBeanInstanceResolver(BeanInstanceResolver resolver)
    {
        this.resolver = resolver;
        return this;
    }
    
    /**
     * 返回Bean的实例
     * 
     * @return
     */
    public Object getBeanInstance()
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
    
    private Object buildInstance()
    {
        Map<String, Object> map = tmpBeanInstanceMap.get();
        boolean cleanMark = map.isEmpty();
        Object instance = null;
        if (isNeedEnhance() && orderedAopManagers.length != 0)
        {
            try
            {
                SetHost newInstance = (SetHost) enhanceType.newInstance();
                newInstance.setHost(resolver.buildInstance());
                instance = newInstance;
                map.put(beanName, instance);
                for (AopManager each : orderedAopManagers)
                {
                    each.fillBean(instance);
                }
            }
            catch (Throwable e)
            {
                throw new NewBeanInstanceException(e);
            }
        }
        if (instance == null)
        {
            instance = resolver.buildInstance();
            map.put(beanName, instance);
        }
        if (injectHandlers.length != 0)
        {
            for (InjectHandler each : injectHandlers)
            {
                each.inject(instance);
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
    
    private void setNeedEnhance(boolean flag)
    {
        state = flag ? state | NEED_ENHANCE : state & (~NEED_ENHANCE);
    }
    
    public void setPrototype(boolean flag)
    {
        state = flag ? state | PROTOTYPE : state & (~PROTOTYPE);
    }
    
    public void setPostConstruct(boolean flag)
    {
        state = flag ? state | POST_CONSTRUCT : state & (~POST_CONSTRUCT);
    }
    
    private boolean isPropertype()
    {
        return (state & PROTOTYPE) != 0;
    }
    
    private boolean isNeedEnhance()
    {
        return (state & NEED_ENHANCE) != 0;
    }
    
    public String getBeanName()
    {
        return beanName;
    }
    
    public Class<?> getType()
    {
        return type;
    }
    
    public void addAopManager(AopManager aopManager)
    {
        aopManagers.add(aopManager);
        setNeedEnhance(true);
    }
}
