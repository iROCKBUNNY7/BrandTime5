package com.jfirer.jfire.core.beanfactory;

import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.BeanFactory;
import com.jfirer.jfire.core.beandescriptor.BeanDescriptor;

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
