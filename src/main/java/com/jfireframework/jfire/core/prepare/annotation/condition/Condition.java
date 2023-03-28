package com.jfireframework.jfire.core.prepare.annotation.condition;

import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.JfireContext;

public interface Condition
{
    boolean match(JfireContext context, AnnotationContext annotationContext, ErrorMessage errorMessage);
}
