package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.util.PrepareConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class EnableAutoConfigurationProcessor implements ContextPrepare
{
    private static final Logger logger        = LoggerFactory.getLogger(EnableAutoConfigurationProcessor.class);
    private static final String directoryName = "META-INF/autoconfig/";
    private static final int    offset        = directoryName.length();
    private static final Logger LOGGER        = LoggerFactory.getLogger(EnableAutoConfigurationProcessor.class);

    @Override
    public ApplicationContext.NeedRefresh prepare(ApplicationContext context)
    {
        try
        {
            ClassLoader                    classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL>               resources   = classLoader.getResources(directoryName);
            ApplicationContext.NeedRefresh needRefresh = ApplicationContext.NeedRefresh.NO;
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
                            needRefresh = registgerAutoConfigor(value, context) ? ApplicationContext.NeedRefresh.YES : needRefresh;
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
                                needRefresh = registgerAutoConfigor(value, context) ? ApplicationContext.NeedRefresh.YES : needRefresh;
                            }
                        }
                    }
                }
            }
            if (needRefresh == ApplicationContext.NeedRefresh.YES)
            {
                LOGGER.debug("traceId:{} 自动注册过程发现有实现了ContextPrepare接口或者注解了@Configuration的Bean，需要刷新容器", TRACEID.currentTraceId());
            }
            return needRefresh;
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return ApplicationContext.NeedRefresh.NO;
        }
    }

    @Override
    public int order()
    {
        return PrepareConstant.ENABLE_AUTO_CONFIGURATION;
    }

    /**
     * 将自动注册类执行注册。如果注册结果是Bean或者没有注册，则返回false，意味着不需要刷新。如果是配置或者是准备接口，则返回true，意味着需要刷新。
     *
     * @param className
     * @param context
     * @return
     * @throws ClassNotFoundException
     */
    boolean registgerAutoConfigor(String className, ApplicationContext context) throws ClassNotFoundException
    {
        String   traceId  = TRACEID.currentTraceId();
        Class<?> configor = Thread.currentThread().getContextClassLoader().loadClass(className);
        switch (context.register(configor))
        {
            case CONFIGURATION:
                LOGGER.debug("traceId:{} 注册自动配置类:{}，其注解@Configuration。", traceId, className);
                return true;
            case PREPARE:
                logger.debug("traceId:{} 注册自动配置类:{},其实现了ContextPrepare接口", traceId, className);
                return true;
            case BEAN:
                LOGGER.debug("traceId:{} 注册自动配置类:{}", traceId, className);
                return false;
            case NODATA:
                LOGGER.debug("traceId:{} 类:{}已经存在，本次自动注册忽略", traceId, className);
            default:
                return false;
        }
    }
}
