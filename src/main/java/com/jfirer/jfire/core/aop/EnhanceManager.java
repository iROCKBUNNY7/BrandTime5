package com.jfirer.jfire.core.aop;

import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.jfire.core.ApplicationContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 不同的AOP管理类实现不同的增强内容
 *
 * @author linbin
 */
public interface EnhanceManager
{
    // 用来作为AOP时增加的属性命名数字后缀，保证一个类中属性名不会出现重复
    AtomicInteger fieldNameCounter  = new AtomicInteger(0);
    AtomicInteger classNameCounter  = new AtomicInteger(0);
    AtomicInteger methodNameCounter = new AtomicInteger(0);
    AtomicInteger varNameCounter    = new AtomicInteger(0);
    int           DEFAULT           = 100;
    int           TRANSACTION       = 10;
    int           CACHE             = 30;
    int           VALIDATE          = 50;

    /**
     * 扫描环境中所有的BeanDefinition，如果发现其符合增强条件，则将自身放入其AopManager集合中。 该方法仅会在环境初始化时调用一次
     */
    void scan(ApplicationContext context);

    /**
     * 执行增强操作
     *
     * @param classModel    为增强类创建的ClassModel
     * @param type          被增强类
     * @param context       上下文
     * @param hostFieldName 被增强类实例
     */
    EnhanceCallbackForBeanInstance enhance(ClassModel classModel, Class<?> type, ApplicationContext context, String hostFieldName);

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
        void setAopHost(Object instance);
    }
}
