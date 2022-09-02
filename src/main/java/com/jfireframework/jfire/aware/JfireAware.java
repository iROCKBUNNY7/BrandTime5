package com.jfireframework.jfire.aware;

import com.jfireframework.jfire.config.environment.Environment;

public interface JfireAware
{
    /**
     * 在容器启动之前触发
     */
    void awareBeforeInitialization(Environment environment);
    
    /**
     * 在容器构建完毕触发
     * 
     * @param environment
     */
    void awareAfterInitialization(Environment environment);
}
