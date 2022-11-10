package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate;

import java.lang.reflect.Method;

public interface JfireMethodValidator
{
	<T> void validateParameters(T object, Method method, Object[] parameterValues, Class<?>... groups);
	
	<T> void validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups);
}
