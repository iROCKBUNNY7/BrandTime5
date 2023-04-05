package com.jfirer.jfire.test.function.aliastest;

import com.jfirer.baseutil.bytecode.support.OverridesAttribute;

import javax.annotation.Resource;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Resource
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired
{
    @OverridesAttribute(annotation = Resource.class, name = "name") String wiredName();
}
