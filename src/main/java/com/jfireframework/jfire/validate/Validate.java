package com.jfireframework.jfire.validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate
{
    /**
     * 验证的组顺序
     * 
     * @return
     */
    Class<?>[] groups() default {};
}
