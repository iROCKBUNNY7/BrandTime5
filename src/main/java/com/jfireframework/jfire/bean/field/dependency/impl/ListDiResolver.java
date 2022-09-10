package com.jfireframework.jfire.bean.field.dependency.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.bean.field.dependency.DiResolver;

public class ListDiResolver implements DiResolver
{
    private long                 offset;
    private List<BeanDefinition> injectValue;
    
    @SuppressWarnings("unchecked")
    @Override
    public void inject(Object src, Map<String, Object> beanInstanceMap)
    {
        List<Object> list = (List<Object>) unsafe.getObject(src, offset);
        if (list == null)
        {
            list = new LinkedList<Object>();
        }
        for (BeanDefinition each : injectValue)
        {
            list.add(each.getConstructedBean().getInstance(beanInstanceMap));
        }
    }
    
    @Override
    public void initialize(Field field, AnnotationUtil annotationUtil, Map<String, BeanDefinition> beanDefinitions)
    {
        offset = unsafe.objectFieldOffset(field);
        Type type = ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        Class<?> beanInterface = null;
        if (type instanceof Class)
        {
            beanInterface = (Class<?>) type;
        }
        else
        {
            throw new IllegalArgumentException();
        }
        injectValue = new LinkedList<BeanDefinition>();
        for (BeanDefinition each : beanDefinitions.values())
        {
            if (beanInterface.isAssignableFrom(each.getType()))
            {
                injectValue.add(each);
            }
        }
    }
}
