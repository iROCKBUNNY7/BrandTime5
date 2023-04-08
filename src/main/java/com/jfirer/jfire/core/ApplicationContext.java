package com.jfirer.jfire.core;

import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;

import java.lang.annotation.Annotation;
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

    List<BeanDefinition> getBeanDefinitionsByAnnotation(Class<? extends Annotation> ckass);

    /**
     * 刷新上下文
     */
    void refresh();

    ////
    Environment getEnv();

    AnnotationContextFactory getAnnotationContextFactory();
}
