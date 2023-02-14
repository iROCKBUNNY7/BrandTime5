package com.jfireframework.jfire.core.prepare;

import com.jfireframework.jfire.core.Environment;

public interface JfirePrepare
{
    void prepare(Environment environment);

    int order();
}
