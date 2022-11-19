package com.jfireframework.jfire.core;

import java.util.Map;
import com.jfireframework.jfire.kernel.Environment;

public interface BeanInstanceResolver
{
	/**
	 * 生成Bean的实例
	 * 
	 * @param beanInstanceMap
	 * @return
	 */
	Object buildInstance(Class<?> type, Map<String, Object> beanInstanceMap);
	
	/**
	 * 初始化
	 * 
	 * @param environment
	 */
	void initialize(Environment environment);
}
