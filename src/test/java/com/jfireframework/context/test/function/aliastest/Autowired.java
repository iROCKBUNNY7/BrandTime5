package com.jfireframework.context.test.function.aliastest;

import com.jfireframework.baseutil.anno.OverridesAttribute;

import javax.annotation.Resource;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Resource
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired
{
    @OverridesAttribute(annotation = Resource.class, name = "name") String wiredName();
}
