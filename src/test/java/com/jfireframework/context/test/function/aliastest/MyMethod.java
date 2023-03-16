package com.jfireframework.context.test.function.aliastest;

import com.jfireframework.baseutil.bytecode.support.OverridesAttribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@InitMethod(name = "")
public @interface MyMethod
{
    @OverridesAttribute(annotation = InitMethod.class, name = "name") String load();
}
