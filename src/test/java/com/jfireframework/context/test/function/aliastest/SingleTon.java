package com.jfireframework.context.test.function.aliastest;

import com.jfireframework.baseutil.anno.OverridesAttribute;

import javax.annotation.Resource;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Resource(shareable = true)
public @interface SingleTon
{
    @OverridesAttribute(annotation = Resource.class, name = "name") String value() default "";
}
