package com.jfireframework.context.test.function.aop;

import javax.annotation.Resource;
import com.jfireframework.jfire.core.aop.impl.AutoResourceAopManager.AutoResource;

@Resource
public class AcManager implements AutoResource
{
	private boolean	open;
	private boolean	close;
	
	@Override
	public void close()
	{
		close = true;
		System.out.println("关闭资源");
	}
	
	@Override
	public void open()
	{
		open = true;
		System.out.println("打开资源");
	}
	
	public boolean isOpen()
	{
		return open;
	}
	
	public boolean isClose()
	{
		return close;
	}
	
}
