package com.jfireframework.context.test.function.aliastest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.jfireframework.baseutil.anno.OverridesAttribute;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@InitMethod(name = "")
public @interface MyMethod
{
	@OverridesAttribute(annotation = InitMethod.class, name = "name")
	public String load();
}
