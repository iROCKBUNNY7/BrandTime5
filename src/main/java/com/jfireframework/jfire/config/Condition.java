package com.jfireframework.jfire.config;

import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

public interface Condition
{
    boolean match(ReadOnlyEnvironment readOnlyEnvironment, AnnotationUtil annotationUtil);
}
