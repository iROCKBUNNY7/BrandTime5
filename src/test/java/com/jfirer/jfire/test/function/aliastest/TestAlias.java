package com.jfirer.jfire.test.function.aliastest;

import com.jfirer.baseutil.bytecode.support.OverridesAttribute;

import javax.annotation.Resource;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Resource
public @interface TestAlias
{
    @OverridesAttribute(annotation = Resource.class, name = "name") String test() default "test";
}
