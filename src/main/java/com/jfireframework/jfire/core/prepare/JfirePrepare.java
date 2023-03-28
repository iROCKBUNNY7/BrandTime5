package com.jfireframework.jfire.core.prepare;

import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.JfireContext;

public interface JfirePrepare
{
    /**
     * 执行预处理流程，如果返回true意味着继续后续的处理，否则中断当前流程
     *
     * @return
     */
    boolean prepare(JfireContext context);

    int order();
}
