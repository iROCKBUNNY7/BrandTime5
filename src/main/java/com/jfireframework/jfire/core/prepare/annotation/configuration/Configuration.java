package com.jfireframework.jfire.core.prepare.annotation.configuration;

import java.lang.annotation.*;

/**
 * 使用该注解，意味着该类是一个配置类
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration
{

}
