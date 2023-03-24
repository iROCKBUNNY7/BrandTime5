package com.jfireframework.jfire.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;

public class JfireBootstrap
{
    private static final Logger       logger       = LoggerFactory.getLogger(JfireBootstrap.class);
    private              JfireContext jfireContext = new JfireContextImpl();

    public JfireBootstrap()
    {
        this(null, null, null);
    }

    public JfireBootstrap(Class<?> bootStrapClass)
    {
        this(bootStrapClass, null, null);
    }

    public JfireBootstrap(Class<?> bootStrapClass, ClassLoader classLoader)
    {
        this(bootStrapClass, classLoader, null);
    }

    public JfireBootstrap(Class<?> bootStrapClass, ClassLoader classLoader, JavaCompiler compiler)
    {
        if (classLoader != null)
        {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        if (compiler != null)
        {
            jfireContext.setJavaCompiler(compiler);
        }
        if (bootStrapClass != null)
        {
            jfireContext.registerConfiguration(bootStrapClass);
        }
    }

    public ApplicationContext start()
    {
        jfireContext.start();
        return jfireContext;
    }
}
