package com.jfireframework.jfire.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.config.Condition;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional
{
    Class<? extends Condition>[] value();
}
