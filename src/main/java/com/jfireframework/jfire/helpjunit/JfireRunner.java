package com.jfireframework.jfire.helpjunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Properties;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.codejson.JsonTool;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.config.JfireInitializationCfg;

public class JfireRunner extends BlockJUnit4ClassRunner
{
    private Class<?> klass;
    private Jfire    jfire;
    
    public JfireRunner(Class<?> klass) throws InitializationError, URISyntaxException
    {
        super(klass);
        this.klass = klass;
        ConfigPath path = klass.getAnnotation(ConfigPath.class);
        String value = path.value();
        JfireInitializationCfg cfg = null;
        if (value.startsWith("classpath:"))
        {
            cfg = (JfireInitializationCfg) JsonTool.read(JfireInitializationCfg.class, StringUtil.readFromClasspath(value.substring(10), Charset.forName("utf8")));
        }
        else if (value.startsWith("file:"))
        {
            InputStream inputStream = null;
            try
            {
                inputStream = new FileInputStream(new File(value.substring(5)));
                byte[] src = new byte[inputStream.available()];
                inputStream.read(src);
                cfg = (JfireInitializationCfg) JsonTool.read(JfireInitializationCfg.class, new String(src, Charset.forName("utf8")));
            }
            catch (IOException e)
            {
                throw new JustThrowException(e);
            }
            finally
            {
                if (inputStream != null)
                {
                    try
                    {
                        inputStream.close();
                    }
                    catch (IOException e)
                    {
                        throw new JustThrowException(e);
                    }
                }
            }
        }
        JfireConfig jfireConfig = new JfireConfig(cfg);
        jfireConfig.registerBeanDefinition(klass.getName(), false, klass);
        if (klass.isAnnotationPresent(PropertyAdd.class))
        {
            Properties properties = new Properties();
            PropertyAdd add = klass.getAnnotation(PropertyAdd.class);
            for (String each : add.value().split(","))
            {
                String[] tmp = each.split("=");
                properties.put(tmp[0], tmp[1]);
            }
            jfireConfig.addProperties(properties);
        }
        jfire = new Jfire(jfireConfig);
    }
    
    protected Object createTest()
    {
        return jfire.getBean(klass);
    }
}
