package com.jfireframework.jfire.core.beanfactory;

import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;
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

}
