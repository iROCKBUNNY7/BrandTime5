package com.jfireframework.jfire.core.prepare.impl;

import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import com.jfireframework.jfire.util.Utils;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
public @interface PropertyPath
{
    String[] value();

    class PropertyPathProcessor implements JfirePrepare
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
                        IniFile iniFile = Utils.processPath(path);
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

}
