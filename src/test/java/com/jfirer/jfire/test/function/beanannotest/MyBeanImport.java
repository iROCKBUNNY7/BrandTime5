package com.jfirer.jfire.test.function.beanannotest;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.jfire.core.JfireContext;
import com.jfirer.jfire.core.prepare.annotation.configuration.Bean;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;

import javax.annotation.Resource;

@Configuration
public class MyBeanImport
{
    @Resource
    private JfireContext context;

    @Bean(name = "person6")
    public Person importBean()
    {
        AnnotationContextFactory annotationContextFactory = context.getAnnotationContextFactory();
        for (Class<?> each : context.getConfigurationClassSet())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each);
            if (annotationContext.isAnnotationPresent(MyImport.class))
            {
                MyImport myImport = annotationContext.getAnnotation(MyImport.class);
                Person   person   = new Person();
                person.setName(myImport.name());
                return person;
            }
        }
        return null;
    }
}
