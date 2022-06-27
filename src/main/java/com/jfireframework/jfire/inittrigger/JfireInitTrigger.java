package com.jfireframework.jfire.inittrigger;

import com.jfireframework.jfire.config.environment.Environment;

public interface JfireInitTrigger
{
    /**
     * 上下文分析的最后一步被触发
     */
    void trigger(Environment environment);
}
