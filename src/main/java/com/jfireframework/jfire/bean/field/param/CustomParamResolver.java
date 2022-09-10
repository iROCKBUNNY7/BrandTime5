package com.jfireframework.jfire.bean.field.param;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CustomParamResolver
{
    Class<? extends ParamResolver> value();
}
