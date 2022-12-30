package com.jfireframework.context.test.function.beanannotest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.core.prepare.impl.Import;

@Retention(RetentionPolicy.RUNTIME)
@Import(MyBeanImport.class)
public @interface MyImport
{
    public String name();
}
