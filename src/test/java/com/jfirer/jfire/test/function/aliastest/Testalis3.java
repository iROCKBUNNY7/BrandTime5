package com.jfirer.jfire.test.function.aliastest;

import com.jfirer.baseutil.bytecode.support.OverridesAttribute;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@TestAlias
public @interface Testalis3
{
    @OverridesAttribute(annotation = TestAlias.class, name = "test") String t();
}
