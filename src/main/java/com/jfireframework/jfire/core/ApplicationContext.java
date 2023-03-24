package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;

import java.util.Collection;
import java.util.List;

public interface ApplicationContext
{
    <E> E getBean(Class<E> ckass);

    <E> List<E> getBeans(Class<E> ckass);

    <E> E getBean(String beanName);

    BeanFactory getBeanFactory(BeanDescriptor beanDescriptor);

    Environment getEnv();

    AnnotationContextFactory getAnnotationContextFactory();

    void start();

    Collection<BeanDefinition> getAllBeanDefinitions();

    BeanDefinition getBeanDefinition(Class<?> ckass);

    List<BeanDefinition> getBeanDefinitions(Class<?> ckass);

}
