package com.jfireframework.jfire.core;

import java.lang.reflect.Method;

public class BeanDefinition
{
	// 持有@JfirePrepared注解标记
	private static final int		JFIRE_PREPARED_FLAG					= 1 << 0;
	// 持有@AopManager注解标记
	private static final int		AOP_MANAGER_FLAG					= 1 << 1;
	// 多实例标记
	private static final int		PROTOTYPE_FLAG						= 1 << 2;
	// 增强标记
	private static final int		NEED_ENHANCE_FLAG					= 1 << 3;
	// 持有@PostConstruct注解标记
	private static final int		POST_CONSTRUCT_FLAG					= 1 << 4;
	private static final int		INVOKED_JFIRE_PREPARED_METHOD_FLAG	= 1 << 5;
	/******/
	private int						beanFlag							= 0;
	// 该Bean的类
	private Class<?>				type;
	// 增强后的类，如果没有增强标记，该属性为空
	private Class<?>				enhanceType;
	private String					beanName;
	// 标注@PostConstruct的方法
	private Method					postConstructMethod;
	private BeanInstanceResolver	resolver;
	
	/**
	 * 返回Bean的实例
	 * 
	 * @return
	 */
	public Object getBeanInstance()
	{
		return null;
	}
	
	public void setNeedEnhance(boolean flag)
	{
		beanFlag = flag ? beanFlag | NEED_ENHANCE_FLAG : beanFlag & (~NEED_ENHANCE_FLAG);
	}
	
	public void setJfirePrepared(boolean flag)
	{
		beanFlag = flag ? beanFlag | JFIRE_PREPARED_FLAG : beanFlag & (~JFIRE_PREPARED_FLAG);
	}
	
	public void setPrototype(boolean flag)
	{
		beanFlag = flag ? beanFlag | PROTOTYPE_FLAG : beanFlag & (~PROTOTYPE_FLAG);
	}
	
	public void setAopmanager(boolean flag)
	{
		beanFlag = flag ? beanFlag | AOP_MANAGER_FLAG : beanFlag & (~AOP_MANAGER_FLAG);
	}
	
	public void setPostConstruct(boolean flag)
	{
		beanFlag = flag ? beanFlag | POST_CONSTRUCT_FLAG : beanFlag & (~POST_CONSTRUCT_FLAG);
	}
	
	public void setInvokedJfirePreparedMethod(boolean flag)
	{
		beanFlag = flag ? beanFlag | INVOKED_JFIRE_PREPARED_METHOD_FLAG : beanFlag & (~INVOKED_JFIRE_PREPARED_METHOD_FLAG);
	}
	
}
