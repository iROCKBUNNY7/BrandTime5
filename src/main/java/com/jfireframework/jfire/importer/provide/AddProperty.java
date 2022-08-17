package com.jfireframework.jfire.importer.provide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.environment.Environment;
import com.jfireframework.jfire.importer.ImportSelecter;
import com.jfireframework.jfire.importer.provide.AddProperty.ImportProperty;

@Import(ImportProperty.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddProperty
{
    String[] value();
    
    public static class ImportProperty implements ImportSelecter
    {
        
        @Override
        public void importSelect(Environment environment)
        {
            if (environment.isAnnotationPresent(AddProperty.class))
            {
                AddProperty addProperty = environment.getAnnotation(AddProperty.class);
                for (String each : addProperty.value())
                {
                    String[] tmp = each.split("=");
                    String property = tmp[0].trim();
                    String vlaue = tmp[1].trim();
                    environment.putProperty(property, vlaue);
                }
            }
        }
    }
}
