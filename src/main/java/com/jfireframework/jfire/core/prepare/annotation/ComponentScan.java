package com.jfireframework.jfire.core.prepare.annotation;

import java.lang.annotation.*;

/**
 * 用来填充配置文件中packageNames的值
 *
 * @author linbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
public @interface ComponentScan
{
    String[] value();

}
