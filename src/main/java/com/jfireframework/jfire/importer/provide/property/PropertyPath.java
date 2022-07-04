package com.jfireframework.jfire.importer.provide.property;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.config.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Import(PropertyPathImporter.class)
@Configuration
public @interface PropertyPath
{
    public String[] value();
}
