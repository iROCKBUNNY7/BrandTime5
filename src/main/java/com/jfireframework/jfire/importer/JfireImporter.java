package com.jfireframework.jfire.importer;

import com.jfireframework.jfire.config.environment.Environment;

public interface JfireImporter
{
    /**
     * 导入需要的信息到环境中
     */
    void importer(Environment environment);
}
