package com.jfireframework.jfire.core.prepare.annotation.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean
{
    /**
     * bean的名称，如果不填写的话，默认为方法名
     *
     * @return
     */
    String name() default "";

    boolean prototype() default false;

}
