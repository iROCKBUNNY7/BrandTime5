package com.jfireframework.jfire.core;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 依赖注入接口
 * 
 * @author linbin
 *
 */
public interface RefResolver
{
	void init(Field field, Environment environment);
	
	void inject(Object instance, Map<String, Object> beanInstanceMap);
}
