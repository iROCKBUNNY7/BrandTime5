package com.jfireframework.jfire.aware;

import com.jfireframework.baseutil.order.Order;

/**
 * 容器初始化完毕后被调用
 * 
 * @author 林斌
 *
 */
public interface JfireAwareContextInited extends Order
{
    /**
     * 当容器初始化完成后，该接口会被容器调用
     * 
     * @author 林斌(eric@jfire.cn)
     */
    public void awareContextInited();
}
