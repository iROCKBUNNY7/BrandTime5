package com.jfireframework.context.test.function.beanannotest;

import javax.annotation.Resource;
import com.jfireframework.context.test.function.beanannotest.Data.NameProperty;
import com.jfireframework.jfire.bean.annotation.field.PropertyRead;
import com.jfireframework.jfire.config.annotation.Bean;
import com.jfireframework.jfire.config.annotation.Conditional;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.environment.Environment;
import com.jfireframework.jfire.inittrigger.JfireInitTrigger;
import com.jfireframework.jfire.inittrigger.provide.scan.ComponentScan;

@Configuration
@ComponentScan("com.jfireframework.context.test.function.beanannotest")
@MyImport(name = "myimport")
@Import({ HouseProvider.class, NameProperty.class })
public class Data
{
    
    public static class NameProperty implements JfireInitTrigger
    {
        
        @Override
        public void trigger(Environment environment)
        {
            environment.putProperty("name", "linbin");
        }
        
    }
    
    @Resource
    private House house;
    @Resource(name = "house2")
    private House house2;
    
    @Bean
    public Person person()
    {
        return new Person();
    }
    
    @PropertyRead("name")
    private String name;
    
    @Bean
    @Conditional(Person2Condition.class)
    public Person person2()
    {
        return new Person();
    }
    
    @Bean
    public Person person4()
    {
        Person person = new Person();
        person.setName(name);
        return person;
    }
    
    @Bean
    public Person person5()
    {
        Person person = new Person();
        person.setName(house.name());
        return person;
    }
    
    @Bean
    public Person person7()
    {
        Person person = new Person();
        person.setName(house2.name());
        return person;
    }
    
    @Bean
    public Class<Person> person9()
    {
        return Person.class;
    }
}
