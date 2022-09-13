package com.jfireframework.jfire.kernel;

import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;

public interface JfireAwareInitializeFinished
{
    void awareInitializeFinished(ReadOnlyEnvironment readOnlyEnvironment);
}
