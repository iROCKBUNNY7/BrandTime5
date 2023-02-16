package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.IniReader;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.PropertyPath;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

public class PropertyPathProcessor implements JfirePrepare
{

    @Override
    public void prepare(Environment environment)
    {
        if ( environment.isAnnotationPresent(PropertyPath.class) )
        {
            for (PropertyPath propertyPath : environment.getAnnotations(PropertyPath.class))
            {
                for (String path : propertyPath.value())
                {
                    IniReader.IniFile iniFile = Utils.processPath(path);
                    for (String each : iniFile.keySet())
                    {
                        environment.putProperty(each, iniFile.getValue(each));
                    }
                }
            }
        }
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.DEFAULT_ORDER;
    }

}
