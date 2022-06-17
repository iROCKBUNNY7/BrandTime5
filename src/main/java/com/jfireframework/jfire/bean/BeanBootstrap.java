package com.jfireframework.jfire.bean;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import com.jfireframework.jfire.aop.AopUtil;
import com.jfireframework.jfire.aop.EnhanceAnnoInfo;
import com.jfireframework.jfire.bean.field.dependency.DependencyField;
import com.jfireframework.jfire.bean.field.param.ParamField;
import com.jfireframework.jfire.config.BeanInfo;
import sun.reflect.MethodAccessor;

/**
 * 用于承担一个bean初始化中的所有任务
 * 
 * @author 林斌
 *
 */
public interface BeanBootstrap
{
    public String getBeanName();
    
    public Class<?> getType();
    
    public boolean isPrototype();
    
    public void setInjectFields(DependencyField[] injectFields);
    
    public boolean HasFinishAction();
    
    public void addEnhanceBean(Bean bean);
    
    public void setType(Class<?> type);
    
    public Class<?> getOriginType();
    
    public void setParamFields(ParamField[] paramFields);
    
    public void addTxMethod(Method method);
    
    public void addResMethod(Method method);
    
    public List<Method> getTxMethodSet();
    
    public List<Method> getResMethods();
    
    public boolean canEnhance();
    
    public boolean canInject();
    
    public List<EnhanceAnnoInfo> getEnHanceAnnos();
    
    public boolean needEnhance();
    
    public void setPostConstructMethod(MethodAccessor postConstructMethod);
    
    public void addCacheMethod(Method method);
    
    public List<Method> getCacheMethods();
    
    public BeanInfo getBeanInfo();
    
    public void setBeanInfo(BeanInfo beanInfo);
    
    /**
     * 在完成依赖注入，参数注入，初始化方式设置后。所有的bean都会被调用一次该方法。为一些bean可以实现一些特殊的目的
     * 
     * @param beanNameMap
     * @param beanTypeMap
     */
    public void decorateSelf(Map<String, Bean> beanNameMap, Map<Class<?>, Bean> beanTypeMap);
    
    public void setAopUitl(AopUtil aopUtil);
    
    public String[] getMethodParamNames(Method method);
    
    public void setPreDestoryMethod(MethodAccessor preDestoryMethod);
}
