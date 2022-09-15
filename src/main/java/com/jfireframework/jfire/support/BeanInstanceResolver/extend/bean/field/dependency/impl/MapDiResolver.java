package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.field.MapKey;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency.DiResolver;

public class MapDiResolver implements DiResolver
{
    private long                 offset;
    private List<BeanDefinition> injectValue;
    private Method               method;
    private MapKeyType           type;
    
    enum MapKeyType
    {
        method, beanName
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void inject(Object src, Map<String, Object> beanInstanceMap)
    {
        switch (type)
        {
            case method:
            {
                Map<Object, Object> map = (Map<Object, Object>) unsafe.getObject(src, offset);
                if (map == null)
                {
                    map = new HashMap<Object, Object>();
                    unsafe.putObject(src, offset, map);
                }
                Object entryValue;
                Object entryKey;
                for (BeanDefinition each : injectValue)
                {
                    try
                    {
                        entryValue = each.getBeanInstanceResolver().getInstance(beanInstanceMap);
                        entryKey = method.invoke(entryValue);
                        map.put(entryKey, entryValue);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case beanName:
            {
                Map<String, Object> map = (Map<String, Object>) unsafe.getObject(src, offset);
                if (map == null)
                {
                    map = new HashMap<String, Object>();
                    unsafe.putObject(src, offset, map);
                }
                Object entryValue;
                String entryKey;
                for (BeanDefinition each : injectValue)
                {
                    try
                    {
                        entryValue = each.getBeanInstanceResolver().getInstance(beanInstanceMap);
                        entryKey = each.getBeanName();
                        map.put(entryKey, entryValue);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            default:
                break;
        }
        
    }
    
    @Override
    public void initialize(Field field, AnnotationUtil annotationUtil, Map<String, BeanDefinition> beanDefinitions)
    {
        offset = unsafe.objectFieldOffset(field);
        Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        Verify.matchType(types[0], Class.class, "map依赖字段，要求key必须指明类型，而当前类型是{}", types[0]);
        Verify.matchType(types[1], Class.class, "map依赖字段，要求value必须指明类型，而当前类型是{}", types[1]);
        Class<?> valueClass = (Class<?>) (types[1]);
        injectValue = new LinkedList<BeanDefinition>();
        for (BeanDefinition each : beanDefinitions.values())
        {
            if (valueClass.isAssignableFrom(each.getType()))
            {
                injectValue.add(each);
            }
        }
        if (annotationUtil.isPresent(MapKey.class, field))
        {
            type = MapKeyType.method;
            String methodName = annotationUtil.getAnnotation(MapKey.class, field).value();
            try
            {
                method = valueClass.getDeclaredMethod(methodName);
                Verify.notNull(method, "执行Map注入分析发现类:{}不存在方法:{},因此无法完成Map注入", valueClass.getName(), methodName);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        else
        {
            type = MapKeyType.beanName;
        }
    }
}
