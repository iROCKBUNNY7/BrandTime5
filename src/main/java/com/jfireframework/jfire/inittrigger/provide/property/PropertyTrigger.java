package com.jfireframework.jfire.inittrigger.provide.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.config.environment.Environment;
import com.jfireframework.jfire.inittrigger.JfireInitTrigger;

public class PropertyTrigger implements JfireInitTrigger
{
    
    @Override
    public void trigger(Environment environment)
    {
        if (environment.isAnnotationPresent(PropertyPath.class))
        {
            for (PropertyPath propertyPath : environment.getAnnotations(PropertyPath.class))
            {
                for (String path : propertyPath.value())
                {
                    InputStream inputStream = null;
                    try
                    {
                        if (path.startsWith("classpath:"))
                        {
                            path = path.substring(10);
                            if (this.getClass().getClassLoader().getResource(path) == null)
                            {
                                continue;
                            }
                            inputStream = this.getClass().getClassLoader().getResourceAsStream(path);
                        }
                        else if (path.startsWith("file:"))
                        {
                            path = path.substring(5);
                            if (new File(path).exists() == false)
                            {
                                continue;
                            }
                            inputStream = new FileInputStream(new File(path));
                        }
                        else
                        {
                            continue;
                        }
                        Properties properties = new Properties();
                        properties.load(inputStream);
                        for (Entry<Object, Object> entry : properties.entrySet())
                        {
                            environment.putProperty((String) entry.getKey(), (String) entry.getValue());
                        }
                    }
                    catch (Exception e)
                    {
                        throw new JustThrowException(e);
                    }
                    finally
                    {
                        try
                        {
                            if (inputStream != null)
                            {
                                inputStream.close();
                                inputStream = null;
                            }
                        }
                        catch (IOException e)
                        {
                            ;
                        }
                    }
                }
            }
        }
    }
    
}
