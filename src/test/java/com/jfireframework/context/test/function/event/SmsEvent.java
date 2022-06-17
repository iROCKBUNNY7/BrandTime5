package com.jfireframework.context.test.function.event;

import com.jfireframework.coordinator.api.CoordinatorConfig;
import com.jfireframework.coordinator.api.ParallelLevel;

public enum SmsEvent implements CoordinatorConfig
{
    // 欠费
    Arrearage,
    // 停机
    halt;
    
    @Override
    public ParallelLevel parallelLevel()
    {
        return ParallelLevel.PAEALLEL;
    }
    
}
