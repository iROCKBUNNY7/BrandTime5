package com.jfireframework.context.test.function.beanannotest;

import java.lang.reflect.Method;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.jfire.config.Condition;
import com.jfireframework.jfire.config.Environment;

public class Person2Condition implements Condition
{
    @Override
    public boolean match(Environment environment, Method method, AnnotationUtil annotationUtil)
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
