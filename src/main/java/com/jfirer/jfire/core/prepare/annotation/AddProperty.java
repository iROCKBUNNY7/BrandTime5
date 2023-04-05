package com.jfirer.jfire.core.prepare.annotation;

import com.jfirer.jfire.core.prepare.processor.AddPropertyProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AddPropertyProcessor.class)
public @interface AddProperty
{
    String[] value();
}
