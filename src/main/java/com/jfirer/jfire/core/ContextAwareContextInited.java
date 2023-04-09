package com.jfirer.jfire.core;

/**
 * 容器初始化完毕后被调用
 *
 * @author 林斌
 */
public interface ContextAwareContextInited
{

    /**
     * 当容器初始化完成后，该接口会被容器调用
     *
     * @author 林斌(eric @ jfire.cn)
     */
    void aware(ApplicationContext applicationContext);
}
