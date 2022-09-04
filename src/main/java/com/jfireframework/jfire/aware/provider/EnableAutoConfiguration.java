package com.jfireframework.jfire.aware.provider;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.TRACEID;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.aware.JfireAwareBeforeInitialization;
import com.jfireframework.jfire.aware.provider.EnableAutoConfiguration.AutoConfig;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.environment.Environment;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AutoConfig.class)
public @interface EnableAutoConfiguration
{
    class AutoConfig implements JfireAwareBeforeInitialization
    {
        private static final Logger logger        = LoggerFactory.getLogger(AutoConfig.class);
        private static final String directoryName = "META-INF/autoconfig/";
        private static final int    offset        = directoryName.length();
        
        @Override
        public void awareBeforeInitialization(Environment environment)
        {
            if (environment.isAnnotationPresent(EnableAutoConfiguration.class) == false)
            {
                return;
            }
            try
            {
                ClassLoader classLoader = environment.getClassLoader();
                Enumeration<URL> resources = classLoader.getResources(directoryName);
                while (resources.hasMoreElements())
                {
                    URL url = resources.nextElement();
                    if (url.getProtocol().equals("jar"))
                    {
                        JarURLConnection openConnection = (JarURLConnection) url.openConnection();
                        JarFile jarFile = openConnection.getJarFile();
                        Enumeration<JarEntry> entries = jarFile.entries();
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
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
        
        void registgerAutoConfigor(String className, Environment environment) throws ClassNotFoundException
        {
            String traceId = TRACEID.currentTraceId();
            logger.debug("traceId:{} 注册自动配置类:{}", traceId, className);
            Class<?> configor = environment.getClassLoader().loadClass(className);
            environment.registerConfiurationBeanDefinition(configor);
        }
        
    }
}
