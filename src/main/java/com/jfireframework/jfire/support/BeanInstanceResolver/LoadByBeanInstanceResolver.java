package com.jfireframework.jfire.support.BeanInstanceResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import javax.annotation.Resource;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.Utils;
import com.jfireframework.jfire.bean.annotation.LazyInitUniltFirstInvoke;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;

public class LoadByBeanInstanceResolver extends BaseBeanInstanceResolver
{
    
    private final String         factoryBeanName;
    private BeanInstanceResolver factoryBean;
    
    public LoadByBeanInstanceResolver(Class<?> type, String beanName, boolean prototype)
    {
        AnnotationUtil annotationUtil = Utils.getAnnotationUtil();
        prototype = annotationUtil.isPresent(Resource.class, type) ? annotationUtil.getAnnotation(Resource.class, type).shareable() == false : false;
        LoadBy loadBy = annotationUtil.getAnnotation(LoadBy.class, type);
        factoryBeanName = loadBy.factoryBeanName();
        baseInitialize(beanName, type, prototype, annotationUtil.isPresent(LazyInitUniltFirstInvoke.class, type));
    }
    
    @Override
    public void initialize(Map<String, BeanDefinition> definitions)
    {
        BeanDefinition beanDefinition = definitions.get(factoryBeanName);
        Verify.notNull(beanDefinition, "需要的工厂bean:{}不存在", factoryBeanName);
        factoryBean = beanDefinition.getBeanInstanceResolver();
    }
    
    @Override
    protected Object buildInstance(Map<String, Object> beanInstanceMap)
    {
        BeanLoadFactory factory = (BeanLoadFactory) factoryBean.getInstance(beanInstanceMap);
        Object entity = factory.load(type);
        beanInstanceMap.put(beanName, entity);
        return entity;
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Documented
    @Inherited
    public static @interface LoadBy
    {
        /**
         * 可以提供Bean的工厂bean的名称
         * 
         * @return
         */
        public String factoryBeanName();
    }
    
    public static interface BeanLoadFactory
    {
        /**
         * 根据类获得对应的对象
         * 
         * @param ckass
         * @return
         */
        public <T, E extends T> E load(Class<T> ckass);
    }
    
}
