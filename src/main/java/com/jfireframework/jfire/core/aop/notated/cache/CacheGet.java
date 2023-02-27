package com.jfireframework.jfire.core.aop.notated.cache;

import java.lang.annotation.*;

/**
 * 这个注解表示会使用方法的入参作为key的规则参数，方法的返回值值作为缓存值存入
 *
 * @author linbin
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheGet
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
