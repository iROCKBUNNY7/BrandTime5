package com.jfireframework.jfire.core.aop.notated.cache;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheDelete
{
    /**
     * key的规则
     *
     * @return
     */
    String value();

    /**
     * 缓存名称
     *
     * @return
     */
    String cacheName() default "default";

    /**
     * 进行缓存操作的条件
     *
     * @return
     */
    String condition() default "";
}
