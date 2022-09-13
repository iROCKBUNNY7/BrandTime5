package com.jfireframework.context.test.function.beanannotest;

import javax.annotation.Resource;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.support.JfirePrepared.Configuration;
import com.jfireframework.jfire.support.JfirePrepared.SelectImport;
import com.jfireframework.jfire.support.JfirePrepared.Configuration.Bean;

@Configuration
public class MyBeanImport implements SelectImport
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
    public void selectImport(Environment environment)
    {
        environment.putProperty("person8", "insertPerson8");
    }
    
}
