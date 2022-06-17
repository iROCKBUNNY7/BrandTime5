package com.jfireframework.jfire.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来代替配置文件中properties中的作用
 * 
 * @author linbin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
public @interface OutterProperties
{
    public String[] value() default {};
    
    public Property[] properties() default {};
    
    public static @interface Property
    {
        public String name();
        
        public String value();
    }
}
