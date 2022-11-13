package com.jfireframework.context.test.function.aliastest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.baseutil.anno.OverridesAttribute;

@Retention(RetentionPolicy.RUNTIME)
@TestAlias
public @interface Testalis3
{
	@OverridesAttribute(annotation = TestAlias.class, name = "test")
	public String t();
	
}
