package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.AddProperty;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

import java.util.Collection;

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
