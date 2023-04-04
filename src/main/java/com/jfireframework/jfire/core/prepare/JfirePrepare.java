package com.jfireframework.jfire.core.prepare;

import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.JfireContext;

public interface JfirePrepare
{
    /**
     * 执行预处理流程，如果返回true意味着需要中断后续流程，直接刷新容器。
     *
     * @return
     */
    JfireContext.NeedRefresh prepare(JfireContext context);

    int order();
}
