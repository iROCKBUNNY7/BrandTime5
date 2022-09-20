package com.jfireframework.jfire.kernel;

/**
 * 容器初始化完毕后被调用
 * 
 * @author 林斌
 *
 */
public interface JfireAwareContextInited
{
    
    /**
     * 当容器初始化完成后，该接口会被容器调用
     * 
     * @author 林斌(eric@jfire.cn)
     */
    public void awareContextInited();
}
