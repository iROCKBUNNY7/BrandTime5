package com.jfireframework.jfire.support.JfirePrepared;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.kernel.Environment;

@Import(AddProperty.ImportProperty.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddProperty
{
    String[] value();
    
    class ImportProperty implements SelectImport
    {
        
        @Override
        public void selectImport(Environment environment)
        {
            if (environment.isAnnotationPresent(AddProperty.class))
            {
                AddProperty[] addProperties = environment.getAnnotations(AddProperty.class);
                for (AddProperty addProperty : addProperties)
                {
                    for (String each : addProperty.value())
                    {
                        int index = each.indexOf('=');
                        if (index != -1)
                        {
                            String property = each.substring(0, index).trim();
                            String vlaue = each.substring(index + 1).trim();
                            environment.putProperty(property, vlaue);
                        }
                    }
                }
            }
        }
    }
}
