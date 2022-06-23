package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.jfire.config.Condition;
import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

public class Person2Condition implements Condition
{
    @Override
    public boolean match(ReadOnlyEnvironment environment, AnnotationUtil annotationUtil)
    {
        if ("pass".equals(environment.getProperty("person2")))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
