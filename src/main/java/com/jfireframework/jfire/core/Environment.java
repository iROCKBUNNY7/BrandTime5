package com.jfireframework.jfire.core;

import com.jfireframework.baseutil.bytecode.support.AnnotationContext;

public interface Environment
{
    AnnotationContext getBootStarpClassAnnotationContext();

    void putProperty(String property, String vlaue);
}
