package com.jfirer.jfire.core.prepare.annotation;

import com.jfirer.jfire.core.prepare.processor.EnableAutoConfigurationProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableAutoConfigurationProcessor.class)
public @interface EnableAutoConfiguration
{
}
