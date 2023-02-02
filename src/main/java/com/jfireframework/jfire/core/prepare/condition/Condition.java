package com.jfireframework.jfire.core.prepare.condition;

import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;

import java.lang.annotation.Annotation;

public interface Condition
{
    boolean match(ReadOnlyEnvironment readOnlyEnvironment, Annotation[] annotations);
}
