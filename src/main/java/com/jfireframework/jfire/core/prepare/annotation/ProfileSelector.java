package com.jfireframework.jfire.core.prepare.annotation;

import com.jfireframework.jfire.core.prepare.processor.ProfileSelectorProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ProfileSelectorProcessor.class)
public @interface ProfileSelector
{
    String protocol() default "file";

    String prefix() default "application_";

    String activePropertyName = "jfire.profile.active";
}
