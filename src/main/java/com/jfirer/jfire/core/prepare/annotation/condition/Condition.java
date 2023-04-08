package com.jfirer.jfire.core.prepare.annotation.condition;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.ApplicationContext;

public interface Condition
{
    boolean match(ApplicationContext context, AnnotationContext annotationContext, ErrorMessage errorMessage);
}
