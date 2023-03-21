package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.jfire.core.aop.EnhanceManager;
import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;
import com.jfireframework.jfire.core.prepare.JfirePrepare;

import java.util.Collection;

public interface JfireContext extends ApplicationContext
{
    /**
     * 刷新上下文，动作包含：<br/>
     * 1. 清空当前所有的Bean配置,并且注册默认的内置Bean<br/>
     * 2. 读取配置类信息，如果配置类注解了Import注解，则导入对应的类。并且将配置类注册为一个Bean。<br/>
     * 3. 执行当前的预处理器处理链<br/>
     */
    void refresh();

    /**
     * 注册一个类，框架会分析导入类的类型：<br/>
     * 1. 如果实现了JfirePrepare接口，则调用registerJfirePrepare接口进行注册,并且返回1。<br/>
     * 2. 如果类上标记为了Configuration注解，则调用registerConfiguration接口进行注册，并且返回2。<br/>
     * 3. 如果类实现了EnhanceManager接口，则调用registerEnhanceManager接口进行注册，并且返回3。<br/>
     * 4. 如果不是抽象类或者接口，则注册为一个Bean。返回4。<br/>
     * <br/>
     * 如果没有任何步骤生效，则返回-1.
     *
     * @param ckass
     */
    int registerClass(Class<?> ckass);

    /**
     * 解析Ckass，并且生成一个Bean定义。
     * @param ckass
     * @return
     */
    boolean registerBean(Class<?> ckass);

    /**
     * 注册Bean定义，注册成功返回true。如果Bean名称相同，则不再注册，直接返回false。
     *
     * @param beanDefinition
     */
    boolean registerBeanDefinition(BeanDefinition beanDefinition);

    /**
     * 在上下文中注册一个配置类，如果该配置类已经存在了，则返回false。
     *
     * @param ckass
     * @return
     */
    boolean registerConfiguration(Class<?> ckass);

    /**
     * 在上下文中注册一个预处理器，如果该预处理器已经存在了，则返回false。
     *
     * @param ckass
     * @return
     */
    boolean registerJfirePrepare(Class<? extends JfirePrepare> ckass);

    /**
     * 在上下文中注册一个增强管理器,如果该增强管理器已经存在，则返回false。
     *
     * @param ckass
     * @return
     */
    boolean registerEnhanceManager(Class<? extends EnhanceManager> ckass);

    Collection<Class<?>> getConfigurationClassSet();
}
