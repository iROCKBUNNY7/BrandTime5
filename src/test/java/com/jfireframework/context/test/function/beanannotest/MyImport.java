package com.jfireframework.context.test.function.beanannotest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.config.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Import(MyBeanImport.class)
public @interface MyImport
{
    public String name();
}
