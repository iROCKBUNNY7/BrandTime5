package com.jfireframework.jfire.aware;

import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

public interface JfireAwareConstructBeanFinished
{
    void awareConstructBeanFinished(ReadOnlyEnvironment readOnlyEnvironment);
}
