package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.jfire.core.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.core.prepare.annotation.condition.Condition;

import java.lang.annotation.Annotation;

public class Person2Condition implements Condition
{
    @Override
    public boolean match(ReadOnlyEnvironment environment, Annotation[] annotations)
    {
        return "pass".equals(environment.getProperty("person2"));
    }
}
