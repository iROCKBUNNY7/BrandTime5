package com.jfireframework.jfire.core.inject.notated;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
public @interface PropertyRead
{
    /**
     * 表示要读取的属性的名称
     *
     * @return
     */
    String value() default "";
}
