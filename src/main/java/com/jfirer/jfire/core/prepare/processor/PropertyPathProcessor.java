package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.IniReader;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.PropertyPath;
import com.jfirer.jfire.util.PrepareConstant;
import com.jfirer.jfire.util.Utils;

public class PropertyPathProcessor implements ContextPrepare
{

    @Override
    public ApplicationContext.NeedRefresh prepare(ApplicationContext jfireContext)
    {
        ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        AnnotationContextFactory annotationContextFactory = jfireContext.getAnnotationContextFactory();
        for (Class<?> each : jfireContext.getConfigurationClassSet())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each, classLoader);
            if (annotationContext.isAnnotationPresent(PropertyPath.class))
            {
                for (PropertyPath propertyPath : annotationContext.getAnnotations(PropertyPath.class))
                {
                    for (String path : propertyPath.value())
                    {
                        IniReader.IniFile iniFile = Utils.processPath(path);
                        for (String property : iniFile.keySet())
                        {
                            jfireContext.getEnv().putProperty(property, iniFile.getValue(property));
                        }
                    }
                }
            }
        }
        return ApplicationContext.NeedRefresh.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.DEFAULT_ORDER;
    }
}
