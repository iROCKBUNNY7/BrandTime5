package com.jfireframework.jfire.bean.field.dependency;

import java.lang.reflect.Field;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.kernel.BeanDefinition;
import sun.misc.Unsafe;

public interface DiResolver
{
    static final Logger logger = LoggerFactory.getLogger(DiResolver.class);
    static final Unsafe unsafe = ReflectUtil.getUnsafe();
    
    void inject(Object src, Map<String, Object> beanInstanceMap);
    
    void initialize(Field field, AnnotationUtil annotationUtil, Map<String, BeanDefinition> beanDefinitions);
    
}
