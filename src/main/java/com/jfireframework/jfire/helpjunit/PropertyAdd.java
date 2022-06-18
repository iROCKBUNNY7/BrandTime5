package com.jfireframework.jfire.helpjunit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyAdd
{
    public String value();
}
