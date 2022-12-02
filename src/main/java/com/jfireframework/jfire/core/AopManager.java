package com.jfireframework.jfire.core;

import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.smc.model.CompilerModel;

/**
 * 这里面AOP增强使用了一个新的套路。每一个AOP增强如果需要放入填充的属性。则让增强类多实现一个接口，该接口就可以用于属性填充。
 * 具体就在fillBean方法中执行
 * 
 * @author linbin
 *
 */
public interface AopManager
{
    // 用来作为AOP时增加的属性命名数字后缀，保证一个类中属性名不会出现重复
    AtomicInteger fieldNameCounter = new AtomicInteger(0);
    AtomicInteger classNameCounter = new AtomicInteger(0);
    
    /**
     * 扫描环境中所有的BeanDefinition，如果发现其符合增强条件，设定增强标志
     * 
     * @param environment
     */
    void scan(Environment environment);
    
    /**
     * 执行增强操作
     * 
     * @param compilerModel
     * @param environment
     */
    void enhance(CompilerModel compilerModel, Environment environment);
    
    /**
     * 填充Bean当中涉及到的AOP增强属性
     * 
     * @param bean
     * @param environment
     */
    void fillBean(Object bean);
    
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
        void setHost(Object instance);
    }
}
