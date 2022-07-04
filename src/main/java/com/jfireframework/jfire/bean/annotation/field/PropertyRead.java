package com.jfireframework.jfire.bean.annotation.field;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Documented
@Inherited
public @interface PropertyRead
{
    /**
     * 表示要读取的属性的名称
     * 
     * @return
     */
    public String value() default "";
}
