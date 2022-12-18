package com.jfireframework.jfire.kernel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.core.ExtraInfoStore;
import com.jfireframework.jfire.util.Utils;

public class Environment
{
	protected final Map<String, BeanDefinition>	beanDefinitions				= new HashMap<String, BeanDefinition>();
	protected final Map<String, String>			properties					= new HashMap<String, String>();
	protected final Set<Method>					annotationPresentMethods	= new HashSet<Method>();
	protected final Set<Class<?>>				annotationPresentClasses	= new HashSet<Class<?>>();
	private final ReadOnlyEnvironment			readOnlyEnvironment			= new ReadOnlyEnvironment(this);
	private final ExtraInfoStore				extraInfoStore				= new ExtraInfoStore();
	private ClassLoader							classLoader;
	
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}
	
	public ClassLoader getClassLoader()
	{
		if (classLoader == null)
		{
			return Environment.class.getClassLoader();
		}
		else
		{
			return classLoader;
		}
	}
	
	public ExtraInfoStore getExtraInfoStore()
	{
		return extraInfoStore;
	}
	
	public Map<String, String> getProperties()
	{
		return properties;
	}
	
	public Map<String, BeanDefinition> getBeanDefinitions()
	{
		return beanDefinitions;
	}
	
	public void registerBeanDefinition(BeanDefinition beanDefinition)
	{
		beanDefinitions.put(beanDefinition.getBeanName(), beanDefinition);
	}
	
	public static class ReadOnlyEnvironment
	{
		private final Environment host;
		
		public ReadOnlyEnvironment(Environment host)
		{
			this.host = host;
		}
		
		public Collection<BeanDefinition> beanDefinitions()
		{
			return Collections.unmodifiableCollection(host.beanDefinitions.values());
		}
		
		public boolean isAnnotationPresent(Class<? extends Annotation> annoType)
		{
			return host.isAnnotationPresent(annoType);
		}
		
		public <T extends Annotation> T getAnnotation(Class<T> type)
		{
			return host.getAnnotation(type);
		}
		
		public String getProperty(String name)
		{
			return host.getProperty(name);
		}
		
		public boolean hasProperty(String name)
		{
			return host.getProperty(name) != null;
		}
		
		public boolean isBeanDefinitionExist(String beanName)
		{
			return host.getBeanDefinition(beanName) != null;
		}
		
	}
	
	public ReadOnlyEnvironment readOnlyEnvironment()
	{
		return readOnlyEnvironment;
	}
	
	public void addAnnotationPresentClass(Class<?> configClass)
	{
		annotationPresentClasses.add(configClass);
	}
	
	public void addAnnotationPresentClass(Method method)
	{
		annotationPresentMethods.add(method);
	}
	
	public boolean isAnnotationPresent(Class<? extends Annotation> annoType)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		for (Class<?> each : annotationPresentClasses)
		{
			if (annotationUtil.isPresent(annoType, each))
			{
				return true;
			}
		}
		for (Method each : annotationPresentMethods)
		{
			if (annotationUtil.isPresent(annoType, each))
			{
				return true;
			}
		}
		return false;
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> type)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		for (Class<?> each : annotationPresentClasses)
		{
			if (annotationUtil.isPresent(type, each))
			{
				return annotationUtil.getAnnotation(type, each);
			}
		}
		for (Method each : annotationPresentMethods)
		{
			if (annotationUtil.isPresent(type, each))
			{
				return annotationUtil.getAnnotation(type, each);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T[] getAnnotations(Class<T> type)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		List<T> list = new ArrayList<T>();
		for (Class<?> each : annotationPresentClasses)
		{
			if (annotationUtil.isPresent(type, each))
			{
				for (T anno : annotationUtil.getAnnotations(type, each))
				{
					list.add(anno);
				}
			}
		}
		for (Method each : annotationPresentMethods)
		{
			if (annotationUtil.isPresent(type, each))
			{
				for (T anno : annotationUtil.getAnnotations(type, each))
				{
					list.add(anno);
				}
			}
		}
		return list.toArray((T[]) Array.newInstance(type, list.size()));
	}
	
	public BeanDefinition getBeanDefinition(String beanName)
	{
		return beanDefinitions.get(beanName);
	}
	
	public BeanDefinition getBeanDefinition(Class<?> type)
	{
		for (BeanDefinition each : beanDefinitions.values())
		{
			if (type == each.getType())
			{
				return each;
			}
		}
		return null;
	}
	
	public String getProperty(String name)
	{
		return properties.get(name);
	}
	
	public void putProperty(String name, String value)
	{
		properties.put(name, value);
	}
	
	public void removeProperty(String name)
	{
		properties.remove(name);
	}
	
}
