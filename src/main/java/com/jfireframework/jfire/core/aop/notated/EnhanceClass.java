package com.jfireframework.jfire.core.aop.notated;

import javax.annotation.Resource;
import java.lang.annotation.*;

/**
 * 代表该类是一个aop增强类 通过target字符串来匹配需要增强的目标类
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Resource
public @interface EnhanceClass
{
    String value();

    /**
     * AOP执行的顺序。数字越大的越先执行。越先执行的越晚结束
     *
     * @return
     */
    int order() default 0;
}
