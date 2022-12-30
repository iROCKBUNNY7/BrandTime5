package com.jfireframework.context.test.function.beanannotest;

import java.lang.annotation.Annotation;
import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.condition.Condition;

public class Person2Condition implements Condition
{
    @Override
    public boolean match(ReadOnlyEnvironment environment, Annotation[] annotations)
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
