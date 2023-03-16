package com.jfireframework.context.test.function.aliastest;

import com.jfireframework.baseutil.bytecode.support.OverridesAttribute;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@TestAlias
public @interface Testalis3
{
    @OverridesAttribute(annotation = TestAlias.class, name = "test") String t();
}
