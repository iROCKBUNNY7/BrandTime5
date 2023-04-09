package com.jfirer.jfire.core.prepare;

import com.jfirer.jfire.core.ApplicationContext;

/**
 * 实现了该接口的实现类，在容器启动前会直接以反射方式被实例化
 */
public interface ContextPrepare
{
    /**
     * 执行预处理流程，如果返回true意味着需要中断后续流程，直接刷新容器。
     *
     * @return
     */
    ApplicationContext.NeedRefresh prepare(ApplicationContext context);

    int order();
}
