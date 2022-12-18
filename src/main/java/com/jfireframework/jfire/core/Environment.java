package com.jfireframework.jfire.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.jfire.exception.DuplicateBeanNameException;
import com.jfireframework.jfire.util.Utils;

public class Environment
{
	public static final String					ENVIRONMENT_FIELD_NAME	= "environment_jfire_3";
	// 版本。每当有新的Bean被注册进来，则+1
	private int									version					= 0;
	private int									markVersion;
	private final Map<String, BeanDefinition>	beanDefinitions			= new HashMap<String, BeanDefinition>();
	private final Map<String, String>			properties				= new HashMap<String, String>();
	private final Set<Annotation>				annotationStore			= new HashSet<Annotation>();
	private final ReadOnlyEnvironment			readOnlyEnvironment		= new ReadOnlyEnvironment(this);
	private ClassLoader							classLoader				= Environment.class.getClassLoader();
	private List<Method>						methods					= new ArrayList<Method>();
	private int									methodSequence			= 0;
	
	public int registerMethod(Method method)
	{
		int index = methods.indexOf(method);
		if (index != -1)
		{
			return index;
		}
		methods.add(method);
		methodSequence += 1;
		return methodSequence - 1;
	}
	
	public Method getMethod(int sequence)
	{
		return methods.get(sequence);
	}
	
	public void markVersion()
	{
		markVersion = version;
	}
	
	/**
	 * 返回自上次标记后环境是否变化
	 * 
	 * @return
	 */
	public boolean isChanged()
	{
		return markVersion == version;
	}
	
	public Map<String, String> getProperties()
	{
		return properties;
	}
	
	public void registerBeanDefinition(BeanDefinition beanDefinition)
	{
		BeanDefinition pred = beanDefinitions.put(beanDefinition.getBeanName(), beanDefinition);
		if (pred != null)
		{
			throw new DuplicateBeanNameException(pred.getBeanName());
		}
		version += 1;
	}
	
	public void removeBeanDefinition(String beanName)
	{
		beanDefinitions.remove(beanName);
	}
	
	public ReadOnlyEnvironment readOnlyEnvironment()
	{
		return readOnlyEnvironment;
	}
	
	public void addAnnotations(Class<?> configClass)
	{
		for (Annotation each : configClass.getAnnotations())
		{
			annotationStore.add(each);
		}
	}
	
	public Map<String, BeanDefinition> beanDefinitions()
	{
		return readOnlyEnvironment.beanDefinitions;
	}
	
	public void addAnnotations(Method method)
	{
		for (Annotation each : method.getAnnotations())
		{
			annotationStore.add(each);
		}
	}
	
	public boolean isAnnotationPresent(Class<? extends Annotation> annoType)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		for (Annotation each : annotationStore)
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
		for (Annotation each : annotationStore)
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
		for (Annotation each : annotationStore)
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
	
	public List<BeanDefinition> getBeanDefinitionByAbstract(Class<?> type)
	{
		if (type.isInterface() == false && Modifier.isAbstract(type.getModifiers()) == false)
		{
			throw new IllegalArgumentException("该方法参数必须为接口或者抽象类");
		}
		List<BeanDefinition> list = new LinkedList<BeanDefinition>();
		for (BeanDefinition each : beanDefinitions.values())
		{
			if (type.isAssignableFrom(each.getType()))
			{
				list.add(each);
			}
		}
		return list;
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
	
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}
	
	public ClassLoader getClassLoader()
	{
		return classLoader;
	}
	
	public static class ReadOnlyEnvironment
	{
		private final Environment					host;
		protected final Map<String, BeanDefinition>	beanDefinitions;
		protected final Map<String, String>			properties;
		
		public ReadOnlyEnvironment(Environment host)
		{
			this.host = host;
			beanDefinitions = Collections.unmodifiableMap(host.beanDefinitions);
			properties = Collections.unmodifiableMap(host.properties);
		}
		
		public Collection<BeanDefinition> beanDefinitions()
		{
			return beanDefinitions.values();
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
}
