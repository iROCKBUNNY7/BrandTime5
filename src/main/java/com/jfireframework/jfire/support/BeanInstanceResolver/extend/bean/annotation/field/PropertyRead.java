package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.field;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param.ParamResolver;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Documented
@Inherited
public @interface PropertyRead
{
	/**
	 * 表示要读取的属性的名称
	 * 
	 * @return
	 */
	public String value() default "";
	
	/**
	 * 指定该参数注入使用的处理器。如果不填写，由系统自动选择
	 * 
	 * @return
	 */
	public Class<? extends ParamResolver> resolver() default ParamResolver.class;
}
