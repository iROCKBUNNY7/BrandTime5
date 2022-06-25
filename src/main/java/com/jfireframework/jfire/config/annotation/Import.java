package com.jfireframework.jfire.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 用来引入其他的类配置.
 * 
 * @author linbin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import
{
    public Class<?>[] value();
}
