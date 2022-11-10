package com.jfireframework.context.test.function.aliastest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Resource;
import com.jfireframework.baseutil.anno.OverridesAttribute;

@Retention(RetentionPolicy.RUNTIME)
@Resource(shareable = true)
public @interface SingleTon
{
	@OverridesAttribute(annotation = Resource.class, name = "name")
	public String value() default "";
}
