package com.jfireframework.jfire.core.prepare.annotation.condition;

import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.jfire.core.ApplicationContext;

public interface Condition
{
    boolean match(ApplicationContext applicationContext, AnnotationContext annotationContext, ErrorMessage errorMessage);
}
