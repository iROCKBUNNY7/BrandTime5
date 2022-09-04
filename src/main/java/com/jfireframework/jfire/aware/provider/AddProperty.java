package com.jfireframework.jfire.aware.provider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.jfireframework.jfire.aware.JfireAwareBeforeInitialization;
import com.jfireframework.jfire.aware.provider.AddProperty.ImportProperty;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.environment.Environment;

@Import(ImportProperty.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddProperty
{
    String[] value();
    
    public static class ImportProperty implements JfireAwareBeforeInitialization
    {
        
        @Override
        public void awareBeforeInitialization(Environment environment)
        {
            if (environment.isAnnotationPresent(AddProperty.class))
            {
                AddProperty[] addProperties = environment.getAnnotations(AddProperty.class);
                for (AddProperty addProperty : addProperties)
                {
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
}
