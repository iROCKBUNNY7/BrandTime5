package com.jfireframework.jfire.exception;

import java.lang.reflect.Method;

public class MethodParamterNameCanNotFetchException extends RuntimeException
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2689180954230999921L;
	
	public MethodParamterNameCanNotFetchException(Method method)
	{
		super("无法获取到方法" + method.toGenericString() + "的参数名称，请检查编译时是否打开了允许方法入参名称");
	}
}
