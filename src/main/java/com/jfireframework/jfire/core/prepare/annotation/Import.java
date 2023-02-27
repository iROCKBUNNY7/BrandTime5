package com.jfireframework.jfire.core.prepare.annotation;

import java.lang.annotation.*;

/**
 * 用来引入其他的类配置.
 *
 * @author linbin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import
{
    Class<?>[] value();
}
