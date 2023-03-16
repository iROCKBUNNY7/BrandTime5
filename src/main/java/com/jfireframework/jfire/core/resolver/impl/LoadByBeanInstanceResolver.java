package com.jfireframework.jfire.core.resolver.impl;

import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.EnvironmentTmp;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.exception.BeanDefinitionCanNotFindException;
import com.jfireframework.jfire.util.Utils;

import java.lang.annotation.*;

public class LoadByBeanInstanceResolver implements BeanInstanceResolver
{
    private String         factoryBeanName;
    private Class<?>       ckass;
    private BeanDefinition factoryBeanDefinition;

    public LoadByBeanInstanceResolver(Class<?> ckass)
    {
        AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
        if (annotationUtil.isPresent(LoadBy.class, ckass) == false)
        {
            throw new IllegalArgumentException();
        }
        this.ckass = ckass;
        factoryBeanName = annotationUtil.getAnnotation(LoadBy.class, ckass).factoryBeanName();
    }

    @Override
    public Object buildInstance()
    {
        Object beanInstance = factoryBeanDefinition.getBean();
        return ((BeanLoadFactory) beanInstance).load(ckass);
    }

    @Override
    public void init(EnvironmentTmp environment)
    {
        factoryBeanDefinition = environment.getBeanDefinition(factoryBeanName);
        if (factoryBeanDefinition == null)
        {
            throw new BeanDefinitionCanNotFindException(factoryBeanName);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Documented
    @Inherited
    public @interface LoadBy
    {
        /**
         * 可以提供Bean的工厂bean的名称
         *
         * @return
         */
        String factoryBeanName();
    }

    public interface BeanLoadFactory
    {
        /**
         * 根据类获得对应的对象
         *
         * @param <T>
         * @param ckass
         * @return
         */
        <T> T load(Class<T> ckass);
    }
}
