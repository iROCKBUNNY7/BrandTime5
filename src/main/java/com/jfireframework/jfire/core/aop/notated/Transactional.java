package com.jfireframework.jfire.core.aop.notated;

import com.jfireframework.jfire.core.aop.impl.transaction.Propagation;

import java.lang.annotation.*;

/**
 * 使用该注解表明该类的公共方法或者注解方法是一个事务方法
 *
 * @author linbin
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Transactional
{
    int propagation() default Propagation.REQUIRED;
}
