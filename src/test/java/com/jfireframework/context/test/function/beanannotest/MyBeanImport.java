package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.prepare.impl.Configuration;
import com.jfireframework.jfire.core.prepare.impl.Configuration.Bean;

import javax.annotation.Resource;

@Configuration
public class MyBeanImport
{
    @Resource
    private Jfire jfire;

    @Bean(name = "person6")
    public Object importBean()
    {
        MyImport import1 = jfire.getAnnotation(MyImport.class);
        Person person = new Person();
        person.setName(import1.name());
        return person;
    }

}
