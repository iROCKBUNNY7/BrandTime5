package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.param;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UserDefinedResolver
{
    Class<? extends ParamResolver> value();
}
