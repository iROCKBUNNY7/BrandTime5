package com.jfireframework.jfire.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@Documented
public @interface Bean
{
    /**
     * bean的名称，如果不填写的话，默认为方法名
     * 
     * @return
     */
    String name() default "";
    
    boolean prototype() default false;
    
    String destroyMethod() default "";
}
