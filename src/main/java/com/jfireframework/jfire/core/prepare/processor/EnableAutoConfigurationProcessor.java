package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.util.JfirePreparedConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class EnableAutoConfigurationProcessor implements JfirePrepare
{
    private static final Logger logger        = LoggerFactory.getLogger(EnableAutoConfigurationProcessor.class);
    private static final String directoryName = "META-INF/autoconfig/";
    private static final int    offset        = directoryName.length();

    @Override
    public JfireContext.NeedRefresh prepare(JfireContext jfireContext)
    {
        try
        {
            ClassLoader      classLoader         = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources           = classLoader.getResources(directoryName);
            boolean          hasNewConfiguration = false;
            while (resources.hasMoreElements())
            {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("jar"))
                {
                    JarURLConnection      openConnection = (JarURLConnection) url.openConnection();
                    JarFile               jarFile        = openConnection.getJarFile();
                    Enumeration<JarEntry> entries        = jarFile.entries();
                    while (entries.hasMoreElements())
                    {
                        JarEntry nextElement = entries.nextElement();
                        if (nextElement.getName().startsWith(directoryName) && nextElement.isDirectory() == false)
                        {
                            String value = nextElement.getName().substring(offset);
                            hasNewConfiguration = registgerAutoConfigor(value, jfireContext);
                        }
                    }
                }
                else if (url.getProtocol().equals("file"))
                {
                    File file = new File(url.toURI());
                    if (file.isDirectory())
                    {
                        for (File each : file.listFiles())
                        {
                            if (each.isDirectory() == false)
                            {
                                String value = each.getName();
                                hasNewConfiguration = registgerAutoConfigor(value, jfireContext);
                            }
                        }
                    }
                }
            }
            if (hasNewConfiguration)
            {
                return JfireContext.NeedRefresh.YES;
            }
            else
            {
                return JfireContext.NeedRefresh.NO;
            }
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return JfireContext.NeedRefresh.NO;
        }
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.ENABLE_AUTO_CONFIGURATION;
    }

    boolean registgerAutoConfigor(String className, JfireContext jfireContext) throws ClassNotFoundException
    {
        String   traceId  = TRACEID.currentTraceId();
        Class<?> configor = Thread.currentThread().getContextClassLoader().loadClass(className);
        if (jfireContext.registerClass(configor) != JfireContext.RegisterResult.NODATA)
        {
            logger.debug("traceId:{} 自动配置发现配置类:{}", traceId, className);
            return true;
        }
        else
        {
            return false;
        }
    }
}
