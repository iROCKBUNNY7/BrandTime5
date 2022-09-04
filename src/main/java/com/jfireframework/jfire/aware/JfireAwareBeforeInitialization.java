package com.jfireframework.jfire.aware;

import com.jfireframework.jfire.config.environment.Environment;

public interface JfireAwareBeforeInitialization
{
    /**
     * 在容器启动之前触发
     */
    void awareBeforeInitialization(Environment environment);
    
}
