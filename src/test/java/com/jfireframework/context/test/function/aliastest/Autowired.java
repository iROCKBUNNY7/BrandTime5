package com.jfireframework.context.test.function.aliastest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Resource;
import com.jfireframework.baseutil.anno.OverridesAttribute;

@Resource
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired
{
	@OverridesAttribute(annotation = Resource.class, name = "name")
	public String wiredName();
}
