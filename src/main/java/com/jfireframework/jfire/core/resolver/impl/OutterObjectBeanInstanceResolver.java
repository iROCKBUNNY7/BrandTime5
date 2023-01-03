package com.jfireframework.jfire.core.resolver.impl;

import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.resolver.BeanInstanceResolver;

public class OutterObjectBeanInstanceResolver implements BeanInstanceResolver
{
	private final Object instance;
	
	public OutterObjectBeanInstanceResolver(Object instance)
	{
		this.instance = instance;
	}
	
	@Override
	public Object buildInstance()
	{
		return instance;
	}
	
	@Override
	public void init(Environment environment)
	{
		
	}
	
}
