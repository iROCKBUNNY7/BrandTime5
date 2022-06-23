package com.jfireframework.jfire.config;

import java.lang.reflect.Method;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;

public interface Condition
{
    boolean match(Environment environment, Method method, AnnotationUtil annotationUtil);
}
