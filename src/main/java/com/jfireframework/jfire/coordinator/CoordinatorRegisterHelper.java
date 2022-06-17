package com.jfireframework.jfire.coordinator;

import javax.annotation.PostConstruct;
import com.jfireframework.baseutil.PackageScan;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.coordinator.api.CoordinatorConfig;
import com.jfireframework.coordinator.util.CoordinatorHelper;

public class CoordinatorRegisterHelper
{
    protected String path;
    
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String[] events = PackageScan.scan(path);
        for (String each : events)
        {
            try
            {
                Class<?> ckass = classLoader.loadClass(each);
                if (Enum.class.isAssignableFrom(ckass) || CoordinatorConfig.class.isAssignableFrom(ckass))
                {
                    CoordinatorHelper.register((Class<? extends Enum<? extends CoordinatorConfig>>) ckass);
                }
            }
            catch (ClassNotFoundException e)
            {
                throw new JustThrowException(e);
            }
        }
    }
}
