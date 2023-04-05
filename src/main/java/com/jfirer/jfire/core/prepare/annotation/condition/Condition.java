package com.jfirer.jfire.core.prepare.annotation.condition;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.JfireContext;

public interface Condition
{
    boolean match(JfireContext context, AnnotationContext annotationContext, ErrorMessage errorMessage);
}
