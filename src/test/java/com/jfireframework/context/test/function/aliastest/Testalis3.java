package com.jfireframework.context.test.function.aliastest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.baseutil.aliasanno.AliasFor;

@Retention(RetentionPolicy.RUNTIME)
@TestAlias
public @interface Testalis3
{
    @AliasFor(annotation = TestAlias.class, value = "test")
    public String t();
    
}
