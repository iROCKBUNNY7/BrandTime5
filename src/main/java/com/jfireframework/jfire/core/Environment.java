package com.jfireframework.jfire.core;

public interface Environment
{
    void putProperty(String property, String vlaue);

    String getProperty(String propertyName);
}
