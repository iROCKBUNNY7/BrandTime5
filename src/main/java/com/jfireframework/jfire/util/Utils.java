package com.jfireframework.jfire.util;

import com.jfireframework.baseutil.IniReader;
import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.bytecode.support.AnnotationContext;
import com.jfireframework.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.ApplicationContext;
import com.jfireframework.jfire.core.BeanDefinition;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Utils
{

    public static IniFile processPath(String path)
    {
        InputStream inputStream = null;
        try
        {
            if (path.startsWith("classpath:"))
            {
                path = path.substring(10);
                if (Utils.class.getClassLoader().getResource(path) == null)
                {
                    throw new NullPointerException(StringUtil.format("资源:{}不存在", path));
                }
                inputStream = Utils.class.getClassLoader().getResourceAsStream(path);
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
            ReflectUtil.throwException(e);
            return null;
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
            }
        }
    }
}
