package com.jfireframework.jfire.core.prepare;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.jfire.util.JfirePreparedConstant;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JfirePreparedNotated
{
	/**
	 * 返回默认的顺序。标注了该注解的类，需要具备一个静态公共方法prepared.唯一入参为Environment
	 * 
	 * @return
	 */
	int order() default JfirePreparedConstant.DEFAULT_ORDER;
	
}
