package com.jfireframework.jfire.kernel;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.anno.AnnotationUtil;

public class Jfire
{
	protected final Map<String, BeanDefinition>			beanDefinitions;
	protected final AnnotationUtil						annotationUtil	= new AnnotationUtil();
	protected static ThreadLocal<Map<String, Object>>	local			= new ThreadLocal<Map<String, Object>>() {
																			@Override
																			protected Map<String, Object> initialValue()
																			{
																				return new HashMap<String, Object>();
																			}
																		};
	
	public Jfire(Map<String, BeanDefinition> beanDefinitions)
	{
		this.beanDefinitions = beanDefinitions;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(String name)
	{
		BeanDefinition beanDefinition = beanDefinitions.get(name);
		if (beanDefinition != null)
		{
			Map<String, Object> map = local.get();
			boolean needClean = map.isEmpty();
			Object instance = beanDefinition.getBeanInstanceResolver().getInstance(map);
			if (needClean)
			{
				map.clear();
			}
			return (T) instance;
		}
		else
		{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> src)
	{
		BeanDefinition beanDefinition = getBeanDefinition(src);
		if (beanDefinition == null)
		{
			return null;
		}
		Map<String, Object> map = local.get();
		boolean needClean = map.isEmpty();
		T instance = (T) beanDefinition.getBeanInstanceResolver().getInstance(map);
		if (needClean)
		{
			map.clear();
		}
		return instance;
	}
	
	public BeanDefinition getBeanDefinition(Class<?> beanClass)
	{
		if (beanClass.isInterface())
		{
			for (BeanDefinition each : beanDefinitions.values())
			{
				if (beanClass.isAssignableFrom(each.getType()))
				{
					return each;
				}
			}
		}
		else
		{
			for (BeanDefinition each : beanDefinitions.values())
			{
				Class<?> type = each.getType();
				if (type == beanClass)
				{
					return each;
				}
			}
		}
		return null;
	}
	
	public BeanDefinition getBeanDefinition(String resName)
	{
		return beanDefinitions.get(resName);
	}
	
	public BeanDefinition[] getBeanDefinitionByAnnotation(Class<? extends Annotation> annotationType)
	{
		List<BeanDefinition> result = new LinkedList<BeanDefinition>();
		for (BeanDefinition each : beanDefinitions.values())
		{
			if (annotationUtil.isPresent(annotationType, each.getType()))
			{
				result.add(each);
			}
		}
		return result.toArray(new BeanDefinition[result.size()]);
	}
	
	public BeanDefinition[] getBeanDefinitionByInterface(Class<?> type)
	{
		List<BeanDefinition> list = new LinkedList<BeanDefinition>();
		for (BeanDefinition each : beanDefinitions.values())
		{
			if (type.isAssignableFrom(each.getType()))
			{
				list.add(beanDefinitions.get(each.getBeanName()));
			}
		}
		return list.toArray(new BeanDefinition[list.size()]);
	}
	
	/**
	 * 关闭容器。该方法会触发单例bean上的close方法
	 */
	public void close()
	{
		for (BeanDefinition each : beanDefinitions.values())
		{
			each.getBeanInstanceResolver().close();
		}
	}
}
