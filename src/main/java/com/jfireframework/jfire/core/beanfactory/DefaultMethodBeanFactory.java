package com.jfireframework.jfire.core.beanfactory;

import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.beandescriptor.BeanDescriptor;
import com.jfireframework.jfire.core.BeanFactory;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Bean;
import com.jfireframework.jfire.exception.BeanDefinitionCanNotFindException;

import javax.annotation.Resource;
import java.lang.reflect.Method;

public class DefaultMethodBeanFactory implements BeanFactory
{
    @Resource
    private AnnotationContextFactory annotationContextFactory;
    @Resource
    private ApplicationContext       applicationContext;

    @Override
    public <E> E getBean(BeanDescriptor beanDescriptor)
    {
        Method descriptorMethod = beanDescriptor.getDescriptorMethod();
        Object hostBean         = applicationContext.getBean(descriptorMethod.getDeclaringClass());
        if (hostBean == null)
        {
            throw new BeanDefinitionCanNotFindException(descriptorMethod.getDeclaringClass());
        }
        Class<?>[] parameterTypes = descriptorMethod.getParameterTypes();
        Object[]   params         = new Object[parameterTypes.length];
        if (params.length == 0)
        {
            try
            {
                return (E) descriptorMethod.invoke(hostBean, null);
            }
            catch (Throwable e)
            {
                ReflectUtil.throwException(e);
                return null;
            }
        }
        for (int i = 0; i < parameterTypes.length; i++)
        {
            params[i] = applicationContext.getBean(parameterTypes[i]);
        }
        try
        {
            return (E) descriptorMethod.invoke(hostBean, params);
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
        if (beanDescriptor.type() != BeanDescriptor.DescriptorType.METHOD)
        {
            return false;
        }
        Method            descriptorMethod  = beanDescriptor.getDescriptorMethod();
        AnnotationContext annotationContext = annotationContextFactory.get(descriptorMethod, descriptorMethod.getDeclaringClass().getClassLoader());
        if (annotationContext.isAnnotationPresent(Bean.class) == false)
        {
            return false;
        }
        return true;
    }
}
