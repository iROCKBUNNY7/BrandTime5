package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.IniReader;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.PropertyPath;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

import java.util.List;

public class PropertyPathProcessor implements JfirePrepare
{

    @Override
    public boolean prepare(JfireContext jfireContext)
    {
        AnnotationContext bootStarpClassAnnotationContext = jfireContext.getEnv().getBootStarpClassAnnotationContext();
        if (bootStarpClassAnnotationContext.isAnnotationPresent(PropertyPath.class))
        {
            List<PropertyPath> annotations = bootStarpClassAnnotationContext.getAnnotations(PropertyPath.class);
            for (PropertyPath propertyPath : annotations)
            {
                for (String path : propertyPath.value())
                {
                    IniReader.IniFile iniFile = Utils.processPath(path);
                    for (String each : iniFile.keySet())
                    {
                        jfireContext.getEnv().putProperty(each, iniFile.getValue(each));
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
