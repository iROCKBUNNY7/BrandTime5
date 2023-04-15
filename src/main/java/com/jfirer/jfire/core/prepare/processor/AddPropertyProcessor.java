package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.AddProperty;
import com.jfirer.jfire.util.PrepareConstant;

public class AddPropertyProcessor implements ContextPrepare
{

    @Override
    public ApplicationContext.NeedRefresh prepare(ApplicationContext jfireContext)
    {
        AnnotationContextFactory annotationContextFactory = jfireContext.getAnnotationContextFactory();
        for (Class<?> each : jfireContext.getConfigurationClassSet())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each);
            if (annotationContext.isAnnotationPresent(AddProperty.class))
            {
                for (AddProperty addProperty : annotationContext.getAnnotations(AddProperty.class))
                {
                    for (String pair : addProperty.value())
                    {
                        int index = pair.indexOf("=");
                        if (index != -1)
                        {
                            String property = pair.substring(0, index).trim();
                            String value    = pair.substring(index + 1).trim();
                            jfireContext.getEnv().putProperty(property, value);
                        }
                    }
                }
            }
        }
        return ApplicationContext.NeedRefresh.NO;
    }

    public int order()
    {
        return PrepareConstant.DEFAULT_ORDER;
    }
}
