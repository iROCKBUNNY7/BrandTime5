package com.jfireframework.jfire.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * 注入处理器。可能注入的是参数，也可能是依赖
 * 
 * @author linbin
 *
 */
public interface DiHandler
{
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomDiHanlder
	{
		// 自定义的参数注入解析器
		Class<? extends ValueResolver> valueResolver() default ValueResolver.class;
		
		// 自定义的依赖注入解析器
		Class<? extends RefResolver> refResolver() default RefResolver.class;
		
	}
	
	void init(Field field, Environment environment);
	
	void inject(Object instance, Map<String, Object> beanInstanceMap);
}
