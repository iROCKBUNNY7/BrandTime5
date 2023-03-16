package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.jfire.core.Environment;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.AddProperty;
import com.jfireframework.jfire.util.JfirePreparedConstant;

import java.util.List;

public class AddPropertyProcessor implements JfirePrepare
{

    @Override
    public void prepare(JfireContext jfireContext)
    {
        Environment       environment                     = jfireContext.getEnv();
        AnnotationContext bootStarpClassAnnotationContext = environment.getBootStarpClassAnnotationContext();
        if (bootStarpClassAnnotationContext.isAnnotationPresent(AddProperty.class))
        {
            List<AddProperty> addProperties = bootStarpClassAnnotationContext.getAnnotations(AddProperty.class);
            for (AddProperty addProperty : addProperties)
            {
                for (String each : addProperty.value())
                {
                    int index = each.indexOf('=');
                    if (index != -1)
                    {
                        String property = each.substring(0, index).trim();
                        String vlaue    = each.substring(index + 1).trim();
                        environment.putProperty(property, vlaue);
                    }
                }
            }
        }
    }

    public int order()
    {
        return JfirePreparedConstant.DEFAULT_ORDER;
    }
}
