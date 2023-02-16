package com.jfireframework.jfire.core.prepare.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
public @interface PropertyPath
{
    String[] value();

}
