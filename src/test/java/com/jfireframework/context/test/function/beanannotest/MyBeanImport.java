package com.jfireframework.context.test.function.beanannotest;

import javax.annotation.Resource;
import com.jfireframework.jfire.config.Environment;
import com.jfireframework.jfire.config.annotation.Bean;
import com.jfireframework.jfire.config.annotation.Configuration;

@Configuration
public class MyBeanImport
{
    @Resource
    private Environment environment;
    
    @Bean(name = "person6")
    public Object importBean()
    {
        MyImport import1 = environment.getAnnotation(MyImport.class);
        Person person = new Person();
        person.setName(import1.name());
        return person;
    }
    
}
