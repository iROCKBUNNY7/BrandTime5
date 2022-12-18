package com.jfireframework.jfire.support.BeanInstanceResolver;

import java.lang.reflect.Method;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;
import com.jfireframework.jfire.kernel.BeanDefinition;
import com.jfireframework.jfire.kernel.BeanInstanceResolver;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.LazyInitUniltFirstInvoke;
import com.jfireframework.jfire.support.JfirePrepared.configuration.Bean;
import com.jfireframework.jfire.util.Utils;

public class MethodBeanInstanceResolver extends BaseBeanInstanceResolver
{
	
	private Method					method;
	private final Class<?>			type;
	private final String			beanName;
	private BeanInstanceResolver	hostBean;
	private BeanInstanceResolver[]	paramBeans;
	
	public MethodBeanInstanceResolver(Method method)
	{
		this.method = method;
		type = method.getReturnType();
		AnnotationUtil annotationUtil = Utils.ANNOTATION_UTIL;
		Bean bean = annotationUtil.getAnnotation(Bean.class, method);
		beanName = StringUtil.isNotBlank(bean.name()) ? bean.name() : method.getName();
		prototype = bean.prototype();
		if (StringUtil.isNotBlank(bean.destroyMethod()))
		{
			try
			{
				preDestoryMethod = method.getReturnType().getDeclaredMethod(bean.destroyMethod());
				preDestoryMethod.setAccessible(true);
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
		}
		else
		{
			preDestoryMethod = null;
		}
		lazyInitUntilFirstInvoke = annotationUtil.isPresent(LazyInitUniltFirstInvoke.class, method);
		baseInitialize(beanName, type, prototype, lazyInitUntilFirstInvoke);
	}
	
	@Override
	protected Object buildInstance(Map<String, Object> beanInstanceMap)
	{
		try
		{
			Object host = hostBean.getInstance(beanInstanceMap);
			Object[] params = new Object[paramBeans.length];
			for (int i = 0; i < params.length; i++)
			{
				params[i] = paramBeans[i].getInstance(beanInstanceMap);
			}
			Object instance = method.invoke(host, params);
			beanInstanceMap.put(beanName, instance);
			return instance;
		}
		catch (Exception e)
		{
			throw new UnSupportException(StringUtil.format("初始化bean实例错误，实例名称:{},对象类名:{}", beanName, type.getName()), e);
		}
	}
	
	@Override
	public void initialize(Environment environment)
	{
		Map<String, BeanDefinition> definitions = environment.getBeanDefinitions();
		Class<?> hostBeanType = method.getDeclaringClass();
		for (BeanDefinition each : definitions.values())
		{
			if (hostBeanType == each.getType() || hostBeanType == each.getType().getSuperclass())
			{
				hostBean = each.getBeanInstanceResolver();
				break;
			}
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		paramBeans = new BeanInstanceResolver[paramTypes.length];
		for (int i = 0; i < paramBeans.length; i++)
		{
			Class<?> type = paramTypes[i];
			for (BeanDefinition each : definitions.values())
			{
				if (type == each.getType() || type == each.getType().getSuperclass())
				{
					paramBeans[i] = each.getBeanInstanceResolver();
					break;
				}
			}
			if (paramBeans[i] == null)
			{
				throw new NullPointerException();
			}
		}
	}
	
}
