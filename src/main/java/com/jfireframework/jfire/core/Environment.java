package com.jfireframework.jfire.core;

public class Environment
{
	// 版本。每当环境有变化就递增1
	private int	version	= 0;
	private int	markVersion;
	
	public void mark()
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
	
}
