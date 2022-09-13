package com.jfireframework.jfire.support.JfirePrepared.condition;

import java.lang.annotation.Annotation;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;

public interface Condition
{
    boolean match(ReadOnlyEnvironment readOnlyEnvironment, Annotation[] annotations);
}
