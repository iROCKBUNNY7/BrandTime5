package com.jfireframework.jfire.exception;

import java.util.List;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.jfire.core.BeanDefinition;

public class BeanDefinitionCanNotFindException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6930943532766346915L;
	
	public BeanDefinitionCanNotFindException(String beanName)
	{
		super("无法找到bean：" + beanName);
	}
	
	public BeanDefinitionCanNotFindException(List<BeanDefinition> list, Class<?> type)
	{
		super(StringUtil.format("无法找到合适的Bean,符合类型:{}的bean存在:{}个", type.getName(), list.size()));
	}
}
