package com.jfireframework.context.test.function.aliastest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Resource;
import com.jfireframework.baseutil.anno.OverridesAttribute;

@Retention(RetentionPolicy.RUNTIME)
@Resource
public @interface TestAlias
{
	@OverridesAttribute(annotation = Resource.class, name = "name")
	String test() default "test";
	
}
