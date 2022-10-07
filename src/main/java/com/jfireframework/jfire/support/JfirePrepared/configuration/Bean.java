package com.jfireframework.jfire.support.JfirePrepared.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Bean
{
	/**
	 * bean的名称，如果不填写的话，默认为方法名
	 * 
	 * @return
	 */
	String name() default "";
	
	boolean prototype() default false;
	
	String destroyMethod() default "";
}
