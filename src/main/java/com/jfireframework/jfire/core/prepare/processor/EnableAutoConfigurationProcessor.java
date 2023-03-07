package com.jfireframework.jfire.core.prepare.processor;

import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.jfire.core.Environment;
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
    public void prepare(Environment environment)
    {
        try
        {
            ClassLoader      classLoader = environment.getClassLoader();
            Enumeration<URL> resources   = classLoader.getResources(directoryName);
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
                            registgerAutoConfigor(value, environment);
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
                                registgerAutoConfigor(value, environment);
                            }
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
        }
    }

    @Override
    public int order()
    {
        return JfirePreparedConstant.ENABLE_AUTO_CONFIGURATION;
    }

    void registgerAutoConfigor(String className, Environment environment) throws ClassNotFoundException
    {
        String traceId = TRACEID.currentTraceId();
        logger.debug("traceId:{} 发现配置类:{}", traceId, className);
        Class<?> configor = environment.getClassLoader().loadClass(className);
        environment.registerCandidateConfiguration(className);
    }
}
