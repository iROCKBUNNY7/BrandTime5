package com.jfireframework.jfire.condition;

import java.lang.annotation.Annotation;
import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

public interface Condition
{
    boolean match(ReadOnlyEnvironment readOnlyEnvironment, Annotation[] annotations);
}
