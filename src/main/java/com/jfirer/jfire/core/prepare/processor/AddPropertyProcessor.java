package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.JfireContext;
import com.jfirer.jfire.core.prepare.JfirePrepare;
import com.jfirer.jfire.core.prepare.annotation.AddProperty;
import com.jfirer.jfire.util.JfirePreparedConstant;

public class AddPropertyProcessor implements JfirePrepare
{

    @Override
    public JfireContext.NeedRefresh prepare(JfireContext jfireContext)
    {
        AnnotationContextFactory annotationContextFactory = jfireContext.getAnnotationContextFactory();
        ClassLoader              classLoader              = Thread.currentThread().getContextClassLoader();
        for (Class<?> each : jfireContext.getConfigurationClassSet())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each, classLoader);
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
        return JfireContext.NeedRefresh.NO;
    }

    public int order()
    {
        return JfirePreparedConstant.DEFAULT_ORDER;
    }
}
