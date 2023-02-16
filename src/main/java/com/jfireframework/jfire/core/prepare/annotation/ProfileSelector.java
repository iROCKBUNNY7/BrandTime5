package com.jfireframework.jfire.core.prepare.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ProfileSelector
{
    String protocol() default "file:";

    String prefix() default "application_";

    String activePropertyName = "jfire.profile.active";

}
