package com.jfireframework.jfire.core.beanfactory;

import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.BeanDescriptor;
import com.jfireframework.jfire.core.BeanFactory;

import javax.annotation.Resource;
import java.lang.reflect.Modifier;

public class DefaultClassBeanFactory implements BeanFactory
{
    private AnnotationContextFactory annotationContextFactory;

    public DefaultClassBeanFactory(AnnotationContextFactory annotationContextFactory)
    {
        this.annotationContextFactory = annotationContextFactory;
    }

    @Override
    public <E> E getBean(BeanDescriptor beanDescriptor)
    {
        if (beanDescriptor.type() != BeanDescriptor.DescriptorType.CLASS)
        {
            throw new IllegalArgumentException();
        }
        try
        {
            return (E) beanDescriptor.getDescriptorClass().newInstance();
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @Override
    public boolean match(BeanDescriptor beanDescriptor)
    {
        if (beanDescriptor.type() == BeanDescriptor.DescriptorType.METHOD)
        {
            return false;
        }
        Class<?> descriptorClass = beanDescriptor.getDescriptorClass();
        if (descriptorClass.isInterface() || Modifier.isAbstract(descriptorClass.getModifiers()))
        {
            return false;
        }
        if (annotationContextFactory.get(descriptorClass, descriptorClass.getClassLoader()).isAnnotationPresent(Resource.class) == false)
        {
            return false;
        }
        return true;
    }
}
