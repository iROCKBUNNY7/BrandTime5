package com.jfireframework.jfire.core.aop;

import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.jfire.core.Environment;

/**
 * 不同的AOP管理类实现不同的增强内容 具体就在fillBean方法中执行
 * 
 * @author linbin
 *
 */
public interface AopManager
{
    // 用来作为AOP时增加的属性命名数字后缀，保证一个类中属性名不会出现重复
    AtomicInteger fieldNameCounter  = new AtomicInteger(0);
    AtomicInteger classNameCounter  = new AtomicInteger(0);
    AtomicInteger methodNameCounter = new AtomicInteger(0);
    int           DEFAULT           = 100;
    
    /**
     * 扫描环境中所有的BeanDefinition，如果发现其符合增强条件，设定增强标志
     * 
     * @param environment
     */
    void scan(Environment environment);
    
    /**
     * 执行增强操作
     * 
     * @param classModel
     * @param environment
     */
    void enhance(ClassModel classModel, Class<?> type, Environment environment, String hostFieldName);
    
    void enhanceFinish(Class<?> type, Class<?> enhanceType, Environment environment);
    
    /**
     * 填充Bean当中涉及到的AOP增强属性
     * 
     * @param bean
     * @param environment
     */
    void fillBean(Object bean, Class<?> type);
    
    /**
     * 该AOP生效顺序。数字越小生效越快
     * 
     * @return
     */
    int order();
    
    interface SetHost
    {
        /**
         * 设置被代理的实例
         * 
         * @param instance
         */
        void setAopHost(Object instance, Environment environment);
    }
}
