package com.jfireframework.jfire.core.prepare.condition;

import java.lang.annotation.Annotation;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;

public interface Condition
{
	boolean match(ReadOnlyEnvironment readOnlyEnvironment, Annotation[] annotations);
}
