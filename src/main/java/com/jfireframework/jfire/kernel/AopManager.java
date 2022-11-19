package com.jfireframework.jfire.kernel;

public interface AopManager
{
	/**
	 * 扫描环境中所有的BeanDefinition，如果发现其符合增强条件，设定增强标志
	 * @param environment
	 */
	void scan(Environment environment);
}
