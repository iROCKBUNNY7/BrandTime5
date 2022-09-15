package com.jfireframework.jfire.support.BeanInstanceResolver;

import java.lang.reflect.Method;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;
import com.jfireframework.jfire.support.LazyInitHelper;

public abstract class BaseBeanInstanceResolver implements BeanInstanceResolver
{
    protected String              beanName;
    protected Class<?>            type;
    protected boolean             lazyInitUntilFirstInvoke;
    protected boolean             prototype;
    protected LazyInitHelper      lazyInitHelper;
    protected volatile Object     singletonInstance;
    protected Method              preDestoryMethod;
    protected static final Logger logger = LoggerFactory.getLogger(BaseBeanInstanceResolver.class);
    
    public void baseInitialize(String beanName, Class<?> type, boolean prototype, boolean lazyInitUntilFirstInvoke)
    {
        this.beanName = beanName;
        this.type = type;
        this.prototype = prototype;
        this.lazyInitUntilFirstInvoke = lazyInitUntilFirstInvoke;
        if (lazyInitUntilFirstInvoke)
        {
            lazyInitHelper = new LazyInitHelper(prototype, type, beanName) {
                
                @Override
                protected Object buildInstance(Map<String, Object> beanInstanceMap)
                {
                    return BaseBeanInstanceResolver.this.buildInstance(beanInstanceMap);
                }
                
            };
        }
        else
        {
            lazyInitHelper = null;
        }
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
            Object instance = lazyInitHelper.getLazyInitProxyInstance();
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
                synchronized (this)
                {
                    if (singletonInstance == null)
                    {
                        singletonInstance = buildInstance(beanInstanceMap);
                    }
                    return singletonInstance;
                }
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
    
    @Override
    public void close()
    {
        if (prototype == false && preDestoryMethod != null)
        {
            try
            {
                preDestoryMethod.invoke(singletonInstance);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
    }
    
}
