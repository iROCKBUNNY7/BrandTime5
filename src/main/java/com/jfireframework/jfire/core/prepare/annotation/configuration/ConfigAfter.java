package com.jfireframework.jfire.core.prepare.annotation.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用该注解，意味着被注解的Bean首先是一个配置Bean。其次该配置Bean需要在指定的配置Bean之后生效。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigAfter
{
    Class<?> value();
}
