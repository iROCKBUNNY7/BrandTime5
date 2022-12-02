package com.jfireframework.jfire.core.aop.impl;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.aop.AopManager;
import com.jfireframework.jfire.core.aop.AopManagerNotated;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.EnhanceClass;

@AopManagerNotated
public class DefaultAopManager implements AopManager
{
    class EnhanceInfo
    {
        Class<?>         type;
        String[]         fieldNames;
        BeanDefinition[] injects;
        Field[]          fields;
    }
    
    private IdentityHashMap<Class<?>, EnhanceInfo> enhanceInfos = new IdentityHashMap<Class<?>, DefaultAopManager.EnhanceInfo>();
    
    @Override
    public void scan(Environment environment)
    {
        AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
        for (BeanDefinition each : environment.beanDefinitions().values())
        {
            if (annotationUtil.isPresent(EnhanceClass.class, each.getType()))
            {
                String rule = annotationUtil.getAnnotation(EnhanceClass.class, each.getType()).value();
                for (BeanDefinition beanDefinition : environment.beanDefinitions().values())
                {
                    if (StringUtil.match(beanDefinition.getType().getName(), rule))
                    {
                        beanDefinition.addAopManager(this);
                    }
                }
            }
        }
    }
    
    @Override
    public void enhance(ClassModel classModel, Class<?> type, Environment environment)
    {
        
    }
    
    @Override
    public void enhanceFinish(Class<?> type, Class<?> enhanceType, Environment environment)
    {
        EnhanceInfo enhanceInfo = enhanceInfos.get(type);
        Field[] fields = new Field[enhanceInfo.fieldNames.length];
        for (int i = 0; i < enhanceInfo.fieldNames.length; i++)
        {
            String fieldName = enhanceInfo.fieldNames[i];
            try
            {
                Field field = enhanceType.getDeclaredField(fieldName);
                field.setAccessible(true);
                fields[i] = field;
            }
            catch (Exception e)
            {
                throw new CannotFindEnhanceFieldException(e);
            }
        }
        enhanceInfo.fields = fields;
    }
    
    @Override
    public void fillBean(Object bean, Class<?> type)
    {
        EnhanceInfo enhanceInfo = enhanceInfos.get(type);
        Field[] fields = enhanceInfo.fields;
        BeanDefinition[] injects = enhanceInfo.injects;
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            try
            {
                field.set(bean, injects[i].getBeanInstance());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public int order()
    {
        return DEFAULT;
    }
    
}
