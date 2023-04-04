package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.smc.compiler.CompileHelper;
import com.jfireframework.jfire.core.aop.EnhanceManager;
import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;
import com.jfireframework.jfire.core.prepare.JfirePrepare;

import javax.tools.JavaCompiler;
import java.util.Collection;
import java.util.List;

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

    /**
     * 刷新上下文
     */
    void refresh();

    ////
    Environment getEnv();

    AnnotationContextFactory getAnnotationContextFactory();

}
