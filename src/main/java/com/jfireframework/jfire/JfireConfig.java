package com.jfireframework.jfire;

import java.util.Map.Entry;
import java.util.Properties;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.kernel.ExtraInfoStore;
import com.jfireframework.jfire.kernel.Jfire;
import com.jfireframework.jfire.kernel.JfireKernel;
import com.jfireframework.jfire.support.BeanInstanceResolver.LoadByBeanInstanceResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.LoadByBeanInstanceResolver.LoadBy;
import com.jfireframework.jfire.support.BeanInstanceResolver.OutterBeanInstanceResolver;
import com.jfireframework.jfire.support.BeanInstanceResolver.ReflectBeanInstanceResolver;
import com.jfireframework.jfire.support.JfirePrepared.Import;
import com.jfireframework.jfire.support.JfirePrepared.configuration.ProcessConfiguration;

public class JfireConfig
{
	protected ClassLoader			classLoader	= Jfire.class.getClassLoader();
	protected Environment			environment	= new Environment();
	protected static final Logger	logger		= LoggerFactory.getLogger(JfireConfig.class);
	
	public JfireConfig()
	{
	}
	
	public JfireConfig(Class<?> configClass)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		environment.addAnnotationPresentClass(configClass);
		if (annotationUtil.isPresent(Resource.class, configClass))
		{
			registerBeanDefinition(configClass);
		}
	}
	
	public Environment getEnvironment()
	{
		return environment;
	}
	
	public JfireConfig registerBeanDefinition(Class<?>... ckasses)
	{
		for (Class<?> ckass : ckasses)
		{
			buildBeanDefinition(ckass);
		}
		return this;
	}
	
	public JfireConfig registerBeanDefinition(String resourceName, boolean prototype, Class<?> src)
	{
		buildBeanDefinition(resourceName, prototype, src);
		return this;
	}
	
	public JfireConfig registerBeanDefinition(BeanDefinition... definitions)
	{
		for (BeanDefinition definition : definitions)
		{
			environment.registerBeanDefinition(definition);
		}
		return this;
	}
	
	public Jfire build()
	{
		Jfire jfire = new Jfire(environment.getBeanDefinitions());
		environment.setClassLoader(classLoader);
		registerSingletonEntity(ExtraInfoStore.class.getName(), environment.getExtraInfoStore());
		registerSingletonEntity(Jfire.class.getName(), jfire);
		registerSingletonEntity(ClassLoader.class.getName(), classLoader);
		registerSingletonEntity(Environment.class.getName(), environment);
		registerBeanDefinition(Import.ProcessImport.class);
		registerBeanDefinition(ProcessConfiguration.class);
		new JfireKernel().initialize(environment);
		ReflectBeanInstanceResolver.compilers.remove();
		return jfire;
	}
	
	public JfireConfig setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
		Thread.currentThread().setContextClassLoader(classLoader);
		return this;
	}
	
	public JfireConfig addProperties(Properties... properties)
	{
		for (Properties each : properties)
		{
			for (Entry<Object, Object> entry : each.entrySet())
			{
				environment.putProperty((String) entry.getKey(), (String) entry.getValue());
			}
		}
		return this;
	}
	
	public JfireConfig registerSingletonEntity(String beanName, Object entity)
	{
		BeanDefinition beanDefinition = new BeanDefinition(entity.getClass(), new OutterBeanInstanceResolver(beanName, entity));
		environment.registerBeanDefinition(beanDefinition);
		return this;
	}
	
	private BeanDefinition buildBeanDefinition(String beanName, boolean prototype, Class<?> ckass)
	{
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		BeanDefinition beanDefinition;
		if (annotationUtil.isPresent(LoadBy.class, ckass))
		{
			BeanInstanceResolver resolver = new LoadByBeanInstanceResolver(ckass, beanName, prototype);
			beanDefinition = new BeanDefinition(ckass, resolver);
		}
		else if (ckass.isInterface() == false)
		{
			BeanInstanceResolver resolver = new ReflectBeanInstanceResolver(beanName, ckass, prototype);
			beanDefinition = new BeanDefinition(ckass, resolver);
		}
		else
		{
			throw new UnSupportException(StringUtil.format("在接口上只有Resource注解是无法实例化bean的.请检查{}", ckass.getName()));
		}
		environment.registerBeanDefinition(beanDefinition);
		return beanDefinition;
	}
	
	private BeanDefinition buildBeanDefinition(Class<?> ckass)
	{
		BeanInstanceResolver resolver = new ReflectBeanInstanceResolver(ckass);
		BeanDefinition beanDefinition = new BeanDefinition(ckass, resolver);
		environment.registerBeanDefinition(beanDefinition);
		return beanDefinition;
	}
	
}
