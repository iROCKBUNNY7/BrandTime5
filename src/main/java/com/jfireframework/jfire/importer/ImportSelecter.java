package com.jfireframework.jfire.importer;

import com.jfireframework.jfire.config.environment.Environment;

public interface ImportSelecter
{
    /**
     * 导入需要的信息到环境中
     */
    void importSelect(Environment environment);
}
