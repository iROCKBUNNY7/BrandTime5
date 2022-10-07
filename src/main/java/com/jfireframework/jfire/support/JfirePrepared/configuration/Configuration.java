package com.jfireframework.jfire.support.JfirePrepared.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Resource;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Resource
public @interface Configuration
{
	
}
