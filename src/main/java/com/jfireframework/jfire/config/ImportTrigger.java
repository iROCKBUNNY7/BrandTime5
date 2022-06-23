package com.jfireframework.jfire.config;

import com.jfireframework.jfire.config.environment.Environment;

public interface ImportTrigger
{
    /**
     * 上下文分析的最后一步被触发
     */
    void trigger(Environment environment);
}
