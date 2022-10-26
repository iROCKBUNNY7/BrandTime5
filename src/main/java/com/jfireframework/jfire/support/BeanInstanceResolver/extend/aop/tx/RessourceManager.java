package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.tx;

public interface RessourceManager
{
	
	/**
	 * 自动打开一个资源。
	 */
	void open();
	
	/**
	 * 自动关闭对应的资源
	 */
	void close();
}
