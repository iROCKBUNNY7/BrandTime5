package com.jfireframework.jfire.kernel;

import java.util.Map;

public interface BeanInstanceResolver
{
    Object getInstance(Map<String, Object> beanInstanceMap);
    
    void initialize(Environment environment);
    
    /**
     * 当容器被关闭的时候该方法会被调用
     */
    void close();
    
}
