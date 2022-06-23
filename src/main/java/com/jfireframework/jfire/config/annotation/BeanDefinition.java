package com.jfireframework.jfire.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
public @interface BeanDefinition
{
    public String beanName();
    
    public String className() default "";
    
    public String[] dependencies() default {};
    
    public String[] params() default {};
    
    public boolean prototype() default false;
    
    public String postConstructMethod() default "";
    
    public String closeMethod() default "";
    
}
