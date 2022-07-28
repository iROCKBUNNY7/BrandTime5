package com.jfireframework.jfire.bean.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 用于指定顺序
 * 
 * @author 林斌
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Order
{
    int value();
}
