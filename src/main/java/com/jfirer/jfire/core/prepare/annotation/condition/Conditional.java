package com.jfirer.jfire.core.prepare.annotation.condition;

import java.lang.annotation.*;

/**
 * 可以注解在类上或者方法上。注解在类上的时候，意味着该类的所有方法提供的bean都需要满足这个条件。注解在方法上的时候，意味着该方法需要满足具体的条件。
 * 如果类上和方法上都有该注解，首先判断类上的，然后判断方法上的，都满足才允许bean被提供。
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface Conditional
{
    Class<? extends Condition>[] value();
}
