package com.jfirer.jfire.util;

import com.jfirer.baseutil.IniReader;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.reflect.ReflectUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Utils
{

    public static IniReader.IniFile processPath(String path)
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
