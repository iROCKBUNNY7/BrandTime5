package com.jfirer.jfire.core.inject.notated;

import java.lang.annotation.*;

/**
 * 指示map属性字段进行依赖时所需要的信息,可以指定某一个方法的返回值是key
 *
 * @author 林斌{erci@jfire.cn}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
public @interface MapKeyMethodName
{
    String value();
}
