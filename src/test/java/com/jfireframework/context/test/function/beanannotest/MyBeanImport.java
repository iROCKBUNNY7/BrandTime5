package com.jfireframework.context.test.function.beanannotest;

import javax.annotation.Resource;
import com.jfireframework.jfire.config.ImportTrigger;
import com.jfireframework.jfire.config.annotation.Bean;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.config.environment.Environment;

@Configuration
public class MyBeanImport implements ImportTrigger
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
    
    @Bean
    public Person person8()
    {
        Person person = new Person();
        person.setName(environment.getProperty("person8"));
        return person;
    }
    
    @Override
    public void trigger(Environment environment)
    {
        environment.putProperty("person8", "insertPerson8");
    }
    
}
