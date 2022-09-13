package com.jfireframework.context.test.function.beanannotest;

import java.lang.annotation.Annotation;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.support.JfirePrepared.condition.Condition;

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
