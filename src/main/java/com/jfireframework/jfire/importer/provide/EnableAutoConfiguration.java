package com.jfireframework.jfire.importer.provide;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.IniReader;
import com.jfireframework.baseutil.IniReader.IniFile;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.environment.Environment;
import com.jfireframework.jfire.importer.ImportSelecter;
import com.jfireframework.jfire.importer.provide.EnableAutoConfiguration.AutoConfig;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AutoConfig.class)
public @interface EnableAutoConfiguration
{
    class AutoConfig implements ImportSelecter
    {
        private static final Logger logger = LoggerFactory.getLogger(AutoConfig.class);
        
        @Override
        public void importSelect(Environment environment)
        {
            if (environment.isAnnotationPresent(EnableAutoConfiguration.class) == false)
            {
                return;
            }
            String name = "META-INF/jfire.ini";
            try
            {
                ClassLoader classLoader = environment.getClassLoader();
                Enumeration<URL> resources = classLoader.getResources(name);
                while (resources.hasMoreElements())
                {
                    URL url = resources.nextElement();
                    InputStream inputStream = null;
                    try
                    {
                        inputStream = url.openStream();
                        IniFile iniFile = IniReader.read(inputStream, Charset.forName("utf8"));
                        String value = iniFile.getValue("jfire.starter");
                        if (StringUtil.isNotBlank(value))
                        {
                            logger.debug("find:{}", value);
                            for (String each : value.split(","))
                            {
                                Class<?> starter = classLoader.loadClass(each);
                                environment.registerConfiurationBeanDefinition(starter);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        throw new JustThrowException(e);
                    }
                    finally
                    {
                        if (inputStream != null)
                        {
                            inputStream.close();
                        }
                    }
                }
            }
            catch (IOException e)
            {
                throw new JustThrowException(e);
            }
        }
        
    }
}
