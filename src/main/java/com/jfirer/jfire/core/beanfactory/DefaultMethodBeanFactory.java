package com.jfirer.jfire.core.beanfactory;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.BeanFactory;
import com.jfirer.jfire.core.beandescriptor.InstanceDescriptor;
import com.jfirer.jfire.exception.BeanDefinitionCanNotFindException;

import javax.annotation.Resource;
import java.lang.reflect.Method;

public class DefaultMethodBeanFactory implements BeanFactory
{
    @Resource
    private ApplicationContext applicationContext;

    @Override
    public <E> E getInstance(InstanceDescriptor beanDescriptor)
    {
        Method descriptorMethod = (Method) beanDescriptor.newInstanceDescriptor();
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
}
