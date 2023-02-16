package com.jfireframework.jfire.core.prepare.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AddProperty
{
    String[] value();

}
