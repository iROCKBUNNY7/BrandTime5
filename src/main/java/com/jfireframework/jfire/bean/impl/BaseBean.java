package com.jfireframework.jfire.bean.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import com.jfireframework.jfire.bean.Bean;
import com.jfireframework.jfire.bean.field.dependency.DIField;
import com.jfireframework.jfire.bean.field.param.ParamField;
import sun.reflect.MethodAccessor;

public abstract class BaseBean implements Bean
{
    protected MethodAccessor                                    postConstructMethod;
    protected MethodAccessor                                    preDestoryMethod;
    /* bean对象初始化过程中暂存生成的中间对象 */
    protected static final ThreadLocal<HashMap<String, Object>> beanInstanceMap = new ThreadLocal<HashMap<String, Object>>() {
                                                                                    @Override
                                                                                    protected HashMap<String, Object> initialValue()
                                                                                    {
                                                                                        return new HashMap<String, Object>();
                                                                                    }
                                                                                };
    /** 单例的引用对象 */
    protected volatile Object                                   singletonInstance;
    protected ParamField[]                                      paramFields;
    protected DIField[]                                         diFields;
    protected final boolean                                     prototype;
    protected final boolean                                     lazyInitUntilFirstInvoke;
    protected final String                                      beanName;
    protected final Class<?>                                    type;
    protected final LazyInitHelper                              lazyInitHelper;
    
    public BaseBean(Class<?> type, String beanName, boolean prototype, DIField[] diFields, ParamField[] paramFields, boolean lazyInitUntilFirstInvoke)
    {
        this.beanName = beanName;
        this.type = type;
        this.prototype = prototype;
        this.lazyInitUntilFirstInvoke = lazyInitUntilFirstInvoke;
        this.diFields = diFields;
        this.paramFields = paramFields;
        if (lazyInitUntilFirstInvoke)
        {
            lazyInitHelper = new LazyInitHelper();
        }
        else
        {
            lazyInitHelper = null;
        }
    }
    
    public BaseBean(Class<?> type, String beanName, boolean prototype, boolean lazyInitUntilFirstInvoke)
    {
        this.beanName = beanName;
        this.type = type;
        this.prototype = prototype;
        this.lazyInitUntilFirstInvoke = lazyInitUntilFirstInvoke;
        diFields = null;
        paramFields = null;
        if (lazyInitUntilFirstInvoke)
        {
            lazyInitHelper = new LazyInitHelper();
        }
        else
        {
            lazyInitHelper = null;
        }
    }
    
    public void setPostConstructMethod(MethodAccessor postConstructMethod)
    {
        this.postConstructMethod = postConstructMethod;
    }
    
    public void setPreDestoryMethod(MethodAccessor preDestoryMethod)
    {
        this.preDestoryMethod = preDestoryMethod;
    }
    
    @Override
    public Class<?> getType()
    {
        return type;
    }
    
    @Override
    public void close()
    {
        if (prototype == false && preDestoryMethod != null)
        {
            try
            {
                preDestoryMethod.invoke(singletonInstance, null);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
    }
    
    @Override
    public Object getInstance()
    {
        HashMap<String, Object> map = beanInstanceMap.get();
        map.clear();
        return getInstance(map);
        
    }
    
    @Override
    public Object getInstance(Map<String, Object> beanInstanceMap)
    {
        if (beanInstanceMap.containsKey(beanName))
        {
            return beanInstanceMap.get(beanName);
        }
        if (lazyInitUntilFirstInvoke)
        {
            return getInstanceLazy(beanInstanceMap);
        }
        else
        {
            return getInstanceRightNow(beanInstanceMap);
        }
    }
    
    private Object getInstanceLazy(Map<String, Object> beanInstanceMap)
    {
        try
        {
            Object instance;
            instance = lazyInitHelper.generateLazyInitProxyInstance();
            beanInstanceMap.put(beanName, instance);
            return instance;
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    private Object getInstanceRightNow(Map<String, Object> beanInstanceMap)
    {
        if (prototype == false)
        {
            if (singletonInstance == null)
            {
                initSingletonInstance(beanInstanceMap);
                return singletonInstance;
            }
            else
            {
                return singletonInstance;
            }
        }
        else
        {
            return buildInstance(beanInstanceMap);
        }
    }
    
    protected abstract Object buildInstance(Map<String, Object> beanInstanceMap);
    
    protected synchronized void initSingletonInstance(Map<String, Object> beanInstanceMap)
    {
        try
        {
            if (singletonInstance == null)
            {
                singletonInstance = buildInstance(beanInstanceMap);
            }
        }
        catch (Exception e)
        {
            throw new UnSupportException(StringUtil.format("初始化bean实例错误，实例名称:{},对象类名:{}", beanName, type.getName()), e);
        }
    }
    
    private static final AtomicInteger typeCount = new AtomicInteger(1);
    
    public class LazyInitHelper
    {
        private final Constructor<?> proxyConstructor;
        
        public LazyInitHelper()
        {
            proxyConstructor = generateLazyInitProxy();
        }
        
        public Object truelyInitAndGet()
        {
            HashMap<String, Object> map = beanInstanceMap.get();
            map.clear();
            if (prototype == false)
            {
                if (singletonInstance == null)
                {
                    initSingletonInstance(map);
                    return singletonInstance;
                }
                else
                {
                    return singletonInstance;
                }
            }
            else
            {
                return buildInstance(map);
            }
        }
        
        public Object generateLazyInitProxyInstance()
        {
            try
            {
                return proxyConstructor.newInstance(LazyInitHelper.this);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        
        private Constructor<?> generateLazyInitProxy()
        {
            CompilerModel compilerModle = new CompilerModel(type.getSimpleName() + "_LazyInitHelper_" + typeCount.incrementAndGet(), type);
            compilerModle.addField(new FieldModel("proxy", LazyInitHelper.class));
            compilerModle.addConstructor("proxy = $0;", LazyInitHelper.class);
            for (Method method : ReflectUtil.getAllMehtods(type))
            {
                if (Modifier.isPublic(method.getModifiers()))
                {
                    MethodModel methodModel = new MethodModel(method);
                    String body = SmcHelper.getTypeName(type) + " instance = (" + SmcHelper.getTypeName(type) + ")proxy.truelyInitAndGet();\r\n";
                    if (method.getReturnType() == void.class)
                    {
                        body += "instance." + methodModel.getInvokeInfo() + ";";
                        methodModel.setBody(body);
                    }
                    else
                    {
                        body += "return instance." + methodModel.getInvokeInfo() + ";";
                        methodModel.setBody(body);
                    }
                    compilerModle.putMethod(method, methodModel);
                }
            }
            JavaStringCompiler compiler = new JavaStringCompiler();
            try
            {
                Class<?> result = compiler.compile(compilerModle, type.getClassLoader());
                Constructor<?> constructor = result.getConstructor(LazyInitHelper.class);
                return constructor;
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
            
        }
        
    }
}
