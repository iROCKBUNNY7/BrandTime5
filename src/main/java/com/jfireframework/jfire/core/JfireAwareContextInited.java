package com.jfireframework.jfire.core;

import com.jfireframework.jfire.core.EnvironmentTmp.ReadOnlyEnvironment;

/**
 * 容器初始化完毕后被调用
 *
 * @author 林斌
 */
public interface JfireAwareContextInited
{

    /**
     * 当容器初始化完成后，该接口会被容器调用
     *
     * @author 林斌(eric @ jfire.cn)
     */
    void awareContextInited(ReadOnlyEnvironment readOnlyEnvironment);
}
