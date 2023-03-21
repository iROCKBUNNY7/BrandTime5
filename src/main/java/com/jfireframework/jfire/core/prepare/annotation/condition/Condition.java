package com.jfireframework.jfire.core.prepare.annotation.condition;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.EnvironmentTmp.ReadOnlyEnvironment;

import java.util.List;

public interface Condition
{
    boolean match(ApplicationContext applicationContext, AnnotationContext annotationContext, ErrorMessage errorMessage);
}
