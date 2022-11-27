package com.jfireframework.jfire.core;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 参数注入解析器
 * 
 * @author linbin
 *
 */
public interface ValueResolver
{
	void init(Field field, Environment environment);
	
	void inject(Object instance, Map<String, Object> beanInstanceMap);
}
