package com.jfireframework.jfire.bean.field.dependency.impl;

import java.lang.reflect.Field;
import java.util.Map;
import javax.annotation.Resource;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.bean.BeanDefinition;
import com.jfireframework.jfire.bean.annotation.field.CanBeNull;
import com.jfireframework.jfire.bean.field.dependency.DiResolver;

public class InterfaceDiResolver implements DiResolver
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
        Class<?> type = field.getType();
        if (resource.name().equals("") == false)
        {
            BeanDefinition nameBean = beanDefinitions.get(resource.name());
            if (annotationUtil.isPresent(CanBeNull.class, field) && nameBean == null)
            {
                injectValue = null;
            }
            else
            {
                Verify.exist(nameBean, "属性{}.{}指定需要bean:{}注入，但是该bean不存在，请检查", field.getDeclaringClass().getName(), field.getName(), resource.name());
                Verify.True(type.isAssignableFrom(nameBean.getType()), "bean:{}不是接口:{}的实现", nameBean.getType().getName(), type.getName());
                injectValue = nameBean;
            }
        }
        else
        {
            // 寻找实现了该接口的bean,如果超过1个,则抛出异常
            int find = 0;
            BeanDefinition implBean = null;
            for (BeanDefinition each : beanDefinitions.values())
            {
                if (type.isAssignableFrom(each.getType()))
                {
                    find++;
                    if (find > 1)
                    {
                        throw new UnSupportException(StringUtil.format(//
                                "接口或抽象类{}的实现多于一个,无法自动注入{}.{},请在resource注解上注明需要注入的bean的名称.当前发现:{}和{}。", //
                                type.getName(), field.getDeclaringClass().getName(), field.getName(), each.getBeanName(), implBean.getBeanName()));
                    }
                    implBean = each;
                }
            }
            if (find != 0)
            {
                injectValue = implBean;
            }
            else if (annotationUtil.isPresent(CanBeNull.class, field))
            {
                injectValue = null;
            }
            else
            {
                throw new NullPointerException(StringUtil.format("属性{}.{}没有可以注入的bean,属性类型为{}", field.getDeclaringClass().getName(), field.getName(), field.getType().getName()));
            }
        }
    }
}
