package com.jfirer.jfire.core.inject.notated;

import java.lang.annotation.*;

/**
 * 使用该注解表明该属性的注入可以允许为空
 *
 * @author linbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
public @interface CanBeNull
{

}
