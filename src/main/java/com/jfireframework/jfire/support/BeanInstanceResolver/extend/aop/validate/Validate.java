package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;

/**
 * 提供验证组信息
 * 
 * @author 林斌
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface Validate
{
    /**
     * 验证的组顺序
     * 
     * @return
     */
    Class<?>[] groups();
    
}
