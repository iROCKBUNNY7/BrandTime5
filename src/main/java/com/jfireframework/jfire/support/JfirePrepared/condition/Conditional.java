package com.jfireframework.jfire.support.JfirePrepared.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional
{
    Class<? extends Condition>[] value();
}
