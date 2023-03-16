package com.jfireframework.jfire.core.prepare;

import com.jfireframework.jfire.core.JfireContext;

public interface JfirePrepare
{
    void prepare(JfireContext jfireContext);

    int order();
}
