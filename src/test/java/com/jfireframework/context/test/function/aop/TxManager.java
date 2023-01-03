package com.jfireframework.context.test.function.aop;

import javax.annotation.Resource;
import com.jfireframework.jfire.core.aop.impl.TransactionAopManager.TransactionManager;

@Resource
public class TxManager implements TransactionManager
{
	private boolean	beginTransAction;
	private boolean	commit;
	
	public boolean isBeginTransAction()
	{
		return beginTransAction;
	}
	
	public boolean isCommit()
	{
		return commit;
	}
	
	@Override
	public void beginTransAction()
	{
		System.out.println("事务开启");
		beginTransAction = true;
	}
	
	@Override
	public void commit()
	{
		System.out.println("事务结束");
		commit = true;
	}
	
	@Override
	public void rollback(Throwable e)
	{
		System.out.println("事务回滚");
	}
	
}
