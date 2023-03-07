package com.jfireframework.jfire.core.prepare.annotation.condition;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.support.annotaion.AnnotationInstance;

import java.util.List;

public interface Condition
{
    boolean match(ReadOnlyEnvironment readOnlyEnvironment, List<AnnotationInstance> annotationsOnMember, ErrorMessage errorMessage);
}
