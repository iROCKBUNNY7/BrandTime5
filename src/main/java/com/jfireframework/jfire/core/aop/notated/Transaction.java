package com.jfireframework.jfire.core.aop.notated;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.tx.TransactionIsolate;

/**
 * 使用该注解表明该类的公共方法或者注解方法是一个事务方法
 * 
 * @author linbin
 * 
 */
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Transaction
{
    TransactionIsolate isolate() default TransactionIsolate.USE_DB_SETING;
}
