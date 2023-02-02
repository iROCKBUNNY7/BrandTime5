package com.jfireframework.jfire.core.resolver;

import com.jfireframework.jfire.core.Environment;

public interface BeanInstanceResolver
{
    /**
     * 生成Bean的实例
     *
     * @param beanInstanceMap
     * @return
     */
    Object buildInstance();

    /**
     * 当环境稳定下来之后，会执行一次初始化。确保所有的实例生产者可以获取到环境参数
     *
     * @param environment
     */
    void init(Environment environment);

}
