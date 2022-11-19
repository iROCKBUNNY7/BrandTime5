package com.jfireframework.jfire.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BeanInstanceResolverProvider
{
	Class<BeanInstanceResolver> value();
}
