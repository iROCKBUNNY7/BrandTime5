package com.jfireframework.jfire.support;

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
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.FieldModel;
import com.jfireframework.baseutil.smc.model.MethodModel;

public abstract class LazyInitHelper
{
    private final boolean                             prototype;
    private final Class<?>                            originType;
    private final String                              beanName;
    private final Object                              lazyInitProxyInstance;
    private volatile Object                           singletonInstance;
    static final AtomicInteger                        typeCount = new AtomicInteger(1);
    protected static ThreadLocal<Map<String, Object>> local     = new ThreadLocal<Map<String, Object>>() {
                                                                    @Override
                                                                    protected Map<String, Object> initialValue()
                                                                    {
                                                                        return new HashMap<String, Object>();
                                                                    }
                                                                };
    
    public LazyInitHelper(boolean prototype, Class<?> originType, String beanName)
    {
        this.prototype = prototype;
        this.originType = originType;
        this.beanName = beanName;
        try
        {
            lazyInitProxyInstance = generateLazyInitProxy().newInstance(this);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    public Object truelyInitAndGet()
    {
        if (prototype == false)
        {
            if (singletonInstance == null)
            {
                Map<String, Object> map = local.get();
                singletonInstance = initSingletonInstance(map);
                map.clear();
                return singletonInstance;
            }
            else
            {
                return singletonInstance;
            }
        }
        else
        {
            Map<String, Object> map = local.get();
            Object instance = buildInstance(map);
            map.clear();
            return instance;
        }
    }
    
    protected abstract Object buildInstance(Map<String, Object> beanInstanceMap);
    
    protected synchronized Object initSingletonInstance(Map<String, Object> beanInstanceMap)
    {
        try
        {
            if (singletonInstance == null)
            {
                singletonInstance = buildInstance(beanInstanceMap);
            }
            return singletonInstance;
        }
        catch (Exception e)
        {
            throw new UnSupportException(StringUtil.format("初始化bean实例错误，实例名称:{},对象类名:{}", beanName, originType.getName()), e);
        }
    }
    
    public Object getLazyInitProxyInstance()
    {
        try
        {
            return lazyInitProxyInstance;
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    private Constructor<?> generateLazyInitProxy()
    {
        ClassModel compilerModle = new ClassModel(originType.getSimpleName() + "_LazyInitHelper_" + typeCount.incrementAndGet(), originType);
        compilerModle.addField(new FieldModel("proxy", LazyInitHelper.class));
        compilerModle.addConstructor("proxy = $0;", LazyInitHelper.class);
        for (Method method : ReflectUtil.getAllMehtods(originType))
        {
            if (Modifier.isPublic(method.getModifiers()))
            {
                MethodModel methodModel = new MethodModel(method);
                String body = SmcHelper.getTypeName(originType) + " instance = (" + SmcHelper.getTypeName(originType) + ")proxy.truelyInitAndGet();\r\n";
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
            Class<?> result = compiler.compile(compilerModle, originType.getClassLoader());
            Constructor<?> constructor = result.getConstructor(LazyInitHelper.class);
            return constructor;
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        
    }
    
}
