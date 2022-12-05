package com.jfireframework.jfire.core.aop.notated;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Resource;

/**
 * 代表该类是一个aop增强类 通过target字符串来匹配需要增强的目标类
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Resource
public @interface EnhanceClass
{
    String value();
    
    int order() default 0;
}
