package com.jfirer.jfire.core.beanfactory;

import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.BeanFactory;
import com.jfirer.jfire.core.beandescriptor.InstanceDescriptor;

public class DefaultClassBeanFactory implements BeanFactory
{
    private AnnotationContextFactory annotationContextFactory;

    public DefaultClassBeanFactory(AnnotationContextFactory annotationContextFactory)
    {
        this.annotationContextFactory = annotationContextFactory;
    }

    @Override
    public <E> E getInstance(InstanceDescriptor beanDescriptor)
    {
        try
        {
            return (E) ((Class) beanDescriptor.newInstanceDescriptor()).newInstance();
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }
}
