package com.jfireframework.jfire.bean.field.dependency.impl;

import java.lang.reflect.Field;
import java.util.Map;
import javax.annotation.Resource;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.bean.annotation.field.CanBeNull;
import com.jfireframework.jfire.bean.field.dependency.DiResolver;

public class BaseDiResolver implements DiResolver
{
    private long   offset;
    private Object injectValue;
    
    @Override
    public void inject(Object src, Map<String, Object> beanInstanceMap)
    {
        if (injectValue == null)
        {
            return;
        }
        if (injectValue instanceof BeanDefinition)
        {
            Object instance = ((BeanDefinition) injectValue).getConstructedBean().getInstance(beanInstanceMap);
            unsafe.putObject(src, offset, instance);
        }
    }
    
    @Override
    public void initialize(Field field, AnnotationUtil annotationUtil, Map<String, BeanDefinition> beanDefinitions)
    {
        offset = unsafe.objectFieldOffset(field);
        Resource resource = annotationUtil.getAnnotation(Resource.class, field);
        if (resource.name().equals("") == false)
        {
            BeanDefinition nameBean = beanDefinitions.get(resource.name());
            if (nameBean != null)
            {
                Verify.True(field.getType() == nameBean.getType(), "bean:{}不是类:{}的实例", nameBean.getBeanName(), field.getType().getName());
                injectValue = nameBean;
            }
            else
            {
                if (annotationUtil.isPresent(CanBeNull.class, field))
                {
                    injectValue = null;
                }
                else
                {
                    throw new NullPointerException(StringUtil.format("无法注入{}.{},没有任何可以注入的内容", field.getDeclaringClass().getName(), field.getName()));
                }
            }
        }
        else
        {
            String beanName = field.getType().getName();
            BeanDefinition nameBean = beanDefinitions.get(beanName);
            if (nameBean != null)
            {
                Verify.True(field.getType().isAssignableFrom(nameBean.getType()), "bean:{}不是类:{}的实例", nameBean.getBeanName(), field.getType().getName());
                injectValue = nameBean;
                return;
            }
            else
            {
                Class<?> fieldType = field.getType();
                for (BeanDefinition each : beanDefinitions.values())
                {
                    if (fieldType.isAssignableFrom(each.getType()))
                    {
                        injectValue = each;
                        return;
                    }
                }
                if (annotationUtil.isPresent(CanBeNull.class, field))
                {
                    injectValue = null;
                }
                else
                {
                    throw new NullPointerException(StringUtil.format("无法注入{}.{},没有任何可以注入的内容", field.getDeclaringClass().getName(), field.getName()));
                }
            }
        }
    }
}
