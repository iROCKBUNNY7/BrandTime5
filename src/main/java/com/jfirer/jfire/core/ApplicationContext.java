package com.jfirer.jfire.core;

import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.jfire.core.beandescriptor.BeanDescriptor;

import javax.tools.JavaCompiler;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ApplicationContext
{
    <E> E getBean(Class<E> ckass);

    <E> List<E> getBeans(Class<E> ckass);

    <E> E getBean(String beanName);

    /**
     * 提供给接口使用者进行手动注册一个Bean
     *
     * @param ckass
     */
    void register(Class<?> ckass);

    List<BeanDefinition> getBeanDefinitionsByAnnotation(Class<? extends Annotation> ckass);

    /**
     * 刷新上下文，动作包含：<br/>
     * 1. 清空当前所有的Bean配置,并且注册默认的内置Bean<br/>
     * 2. 读取配置类信息，如果配置类注解了Import注解，则导入对应的类。并且将配置类注册为一个Bean。<br/>
     * 3. 执行当前的预处理器处理链<br/>
     * 通过register方法注册的bean不会被删除。
     */
    void refresh();

    ////
    Environment getEnv();

    AnnotationContextFactory getAnnotationContextFactory();

    /**
     * 返回BeanFactory的Bean定义
     *
     * @param beanDescriptor
     * @return
     */
    BeanDefinition getBeanFactory(BeanDescriptor beanDescriptor);

    Collection<BeanDefinition> getAllBeanDefinitions();

    BeanDefinition getBeanDefinition(Class<?> ckass);

    BeanDefinition getBeanDefinition(String beanName);

    List<BeanDefinition> getBeanDefinitions(Class<?> ckass);

    /**
     * 注册一个类，框架会分析导入类的类型：<br/>
     * 1. 如果实现了JfirePrepare接口，则调用registerJfirePrepare接口进行注册。<br/>
     * 2. 如果类上标记为了Configuration注解，则调用registerConfiguration接口进行注册。<br/>
     * 3. 如果不是抽象类或者接口，则注册为一个Bean。<br/>
     * <br/>
     * .
     *
     * @param ckass
     */
    RegisterResult registerClass(Class<?> ckass);

    /**
     * 解析Ckass，并且生成一个Bean定义。
     *
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

    Set<Class<?>> getConfigurationClassSet();

    CompileHelper getCompileHelper();

    enum NeedRefresh
    {
        YES, NO
    }

    enum RegisterResult
    {
        JFIREPREPARE, CONFIGURATION, BEAN, NODATA
    }
}
