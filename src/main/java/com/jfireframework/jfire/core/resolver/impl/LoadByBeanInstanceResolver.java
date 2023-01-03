package com.jfireframework.jfire.core.resolver.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;
import com.jfireframework.jfire.exception.BeanDefinitionCanNotFindException;
import com.jfireframework.jfire.util.Utils;

public class LoadByBeanInstanceResolver implements BeanInstanceResolver
{
	private String			factoryBeanName;
	private Class<?>		ckass;
	private BeanDefinition	factoryBeanDefinition;
	
	public LoadByBeanInstanceResolver(Class<?> ckass)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		if (annotationUtil.isPresent(LoadBy.class, ckass) == false)
		{
			throw new IllegalArgumentException();
		}
		this.ckass = ckass;
		factoryBeanName = annotationUtil.getAnnotation(LoadBy.class, ckass).factoryBeanName();
	}
	
	@Override
	public Object buildInstance()
	{
		Object beanInstance = factoryBeanDefinition.getBeanInstance();
		return ((BeanLoadFactory) beanInstance).load(ckass);
	}
	
	@Override
	public void init(Environment environment)
	{
		factoryBeanDefinition = environment.getBeanDefinition(factoryBeanName);
		if (factoryBeanDefinition == null)
		{
			throw new BeanDefinitionCanNotFindException(factoryBeanName);
		}
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
		 * @param <T>
		 * 
		 * @param ckass
		 * @return
		 */
		public <T> T load(Class<T> ckass);
	}
}
