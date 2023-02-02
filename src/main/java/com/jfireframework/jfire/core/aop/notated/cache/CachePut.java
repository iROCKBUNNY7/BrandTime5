package com.jfireframework.jfire.core.aop.notated.cache;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CachePut
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

    /**
     * 该缓存读取的有效期限。如果是-1，代表缓存不会自动超期释放
     *
     * @return
     */
    int timeToLive() default -1;
}
