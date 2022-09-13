package com.jfireframework.context.test.function.beanannotest;

import javax.annotation.Resource;
import com.jfireframework.context.test.function.beanannotest.Data.NameProperty;
import com.jfireframework.jfire.kernel.Environment;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.field.PropertyRead;
import com.jfireframework.jfire.support.JfirePrepared.ComponentScan;
import com.jfireframework.jfire.support.JfirePrepared.Configuration;
import com.jfireframework.jfire.support.JfirePrepared.Import;
import com.jfireframework.jfire.support.JfirePrepared.SelectImport;
import com.jfireframework.jfire.support.JfirePrepared.Configuration.Bean;
import com.jfireframework.jfire.support.JfirePrepared.condition.Conditional;

@Configuration
@ComponentScan("com.jfireframework.context.test.function.beanannotest")
@MyImport(name = "myimport")
@Import({ HouseProvider.class, NameProperty.class })
public class Data
{
    
    public static class NameProperty implements SelectImport
    {
        
        @Override
        public void selectImport(Environment environment)
        {
            environment.putProperty("name", "linbin");
        }
        
    }
    
    @Resource(name = "house")
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
    
}
