package com.jfireframework.jfire.core;

import com.jfireframework.jfire.core.aop.EnhanceManager;
import com.jfireframework.jfire.core.prepare.JfirePrepare;

public interface JfireContext extends ApplicationContext
{
    /**
     * 刷新上下文，动作包含：
     * 1. 完整执行预处理器
     * 2. 读取配置类信息，注册Bean定义
     */
    void refresh();

    /**
     * 导入一个类，框架会分析导入类的类型：
     * <p>
     * 1. 如果实现了JfirePrepare接口，则调用registerJfirePrepare接口进行注册,并且返回1。<br/>
     * 2. 如果类上标记为了Configuration注解，则调用registerConfiguration接口进行注册，并且返回2。<br/>
     * 3. 如果不是抽象类或者接口，则注册为一个Bean，采用默认的Bean工厂，Bean名称为类全限定名。返回3。<br/>
     * </p>
     * <br/>
     * 如果没有任何步骤生效，则返回-1.
     *
     * @param ckass
     */
    int importClass(Class<?> ckass);

    /**
     * 注册Bean定义，注册成功返回true。如果Bean名称相同，则不再注册，直接返回false。
     *
     * @param beanDefinition
     */
    boolean registerBeanDefinition(BeanDefinition beanDefinition);

    boolean registerConfiguration(Class<?> ckass);

    boolean registerJfirePrepare(Class<? extends JfirePrepare> ckass);

    boolean registerEnhanceManager(Class<? extends EnhanceManager> ckass);

    ////
    void init();

    BeanFactory getBeanFactory(BeanDescriptor beanDescriptor);

    Environment getEnv();
}
