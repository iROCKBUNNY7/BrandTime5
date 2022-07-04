package com.jfireframework.jfire.importer.provide.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.config.environment.Environment;
import com.jfireframework.jfire.importer.JfireImporter;

public class PropertyPathImporter implements JfireImporter
{
    
    @Override
    public void importer(Environment environment)
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
                                throw new NullPointerException(StringUtil.format("资源:{}不存在", path));
                            }
                            inputStream = this.getClass().getClassLoader().getResourceAsStream(path);
                        }
                        else if (path.startsWith("file:"))
                        {
                            path = path.substring(5);
                            if (new File(path).exists() == false)
                            {
                                throw new NullPointerException(StringUtil.format("资源:{}不存在", path));
                            }
                            inputStream = new FileInputStream(new File(path));
                        }
                        else
                        {
                            throw new UnsupportedOperationException("不支持的资源识别前缀:" + path);
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
