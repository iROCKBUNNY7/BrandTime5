package com.jfireframework.jfire.core.resolver.impl;

import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.EnvironmentTmp;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.exception.BeanDefinitionCanNotFindException;
import com.jfireframework.jfire.exception.NewBeanInstanceException;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class MethodBeanInstanceResolver implements BeanInstanceResolver
{
    private Method           method;
    private BeanDefinition[] paramsBeandefinition;
    private BeanDefinition   hostBeanDefinition;

    public MethodBeanInstanceResolver(Method method)
    {
        this.method = method;
        method.setAccessible(true);
    }

    @Override
    public Object buildInstance()
    {
        try
        {
            Object instance = hostBeanDefinition.getBean();
            if (paramsBeandefinition.length == 0)
            {
                return method.invoke(instance);
            }
            else
            {
                Object[] params = new Object[paramsBeandefinition.length];
                for (int i = 0; i < paramsBeandefinition.length; i++)
                {
                    BeanDefinition beanDefinition = paramsBeandefinition[i];
                    params[i] = beanDefinition.getBean();
                }
                return method.invoke(instance, params);
            }
        } catch (Throwable e)
        {
            throw new NewBeanInstanceException(e);
        }
    }

    @Override
    public void init(EnvironmentTmp environment)
    {
        hostBeanDefinition = environment.getBeanDefinition(method.getDeclaringClass());
        if (hostBeanDefinition == null)
        {
            throw new BeanDefinitionCanNotFindException(method.getDeclaringClass());
        }
        List<BeanDefinition> paramsBeandefinition = new LinkedList<BeanDefinition>();
        Class<?>[]           parameterTypes       = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            Class<?> ckass = parameterTypes[i];
            if (ckass.isInterface())
            {
                List<BeanDefinition> list = environment.getBeanDefinitionByAbstract(ckass);
                if (list.isEmpty())
                {
                    throw new BeanDefinitionCanNotFindException(list, ckass);
                }
                paramsBeandefinition.add(list.get(0));
            }
            else
            {
                BeanDefinition beanDefinition = environment.getBeanDefinition(ckass);
                if (beanDefinition == null)
                {
                    throw new BeanDefinitionCanNotFindException(ckass);
                }
                paramsBeandefinition.add(beanDefinition);
            }
        }
        this.paramsBeandefinition = paramsBeandefinition.toArray(new BeanDefinition[paramsBeandefinition.size()]);
    }
}
