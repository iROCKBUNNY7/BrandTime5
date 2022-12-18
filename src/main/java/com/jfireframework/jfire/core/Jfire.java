package com.jfireframework.jfire.core;

import com.jfireframework.jfire.exception.BeanDefinitionCanNotFindException;

public class Jfire
{
	private final Environment environment;
	
	protected Jfire(Environment environment)
	{
		this.environment = environment;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> ckass)
	{
		BeanDefinition beanDefinition = environment.getBeanDefinition(ckass);
		if (beanDefinition == null)
		{
			throw new BeanDefinitionCanNotFindException(ckass);
		}
		return (T) beanDefinition.getBeanInstance();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(String beanName)
	{
		BeanDefinition beanDefinition = environment.getBeanDefinition(beanName);
		if (beanDefinition == null)
		{
			throw new BeanDefinitionCanNotFindException(beanName);
		}
		return (T) beanDefinition.getBeanInstance();
	}
}
