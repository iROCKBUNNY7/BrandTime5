package com.jfireframework.jfire.support.jfireprepared;

import com.jfireframework.baseutil.IniReader;
import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.kernel.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.*;
import java.nio.charset.Charset;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Import(PropertyPath.PropertyPathImporter.class)
public @interface PropertyPath
{
    public String[] value();
    
    public class PropertyPathImporter implements SelectImport
    {
        IniFile processPath(String path)
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
                return IniReader.read(inputStream, Charset.forName("utf8"));
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
        
        @Override
        public void selectImport(Environment environment)
        {
            if (environment.isAnnotationPresent(PropertyPath.class))
            {
                for (PropertyPath propertyPath : environment.getAnnotations(PropertyPath.class))
                {
                    for (String path : propertyPath.value())
                    {
                        IniFile iniFile = processPath(path);
                        for (String each : iniFile.keySet())
                        {
                            environment.putProperty(each, iniFile.getValue(each));
                        }
                    }
                }
            }
        }
        
    }
    
}
