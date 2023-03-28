package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.IniReader;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.PropertyPath;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

public class PropertyPathProcessor implements JfirePrepare
{

    @Override
    public boolean prepare(JfireContext jfireContext)
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
        return true;
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.DEFAULT_ORDER;
    }
}
