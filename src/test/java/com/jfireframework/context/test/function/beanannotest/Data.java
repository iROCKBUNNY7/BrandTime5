package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.jfire.core.JfireContext;
import com.jfireframework.jfire.core.inject.notated.PropertyRead;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.annotation.ComponentScan;
import com.jfireframework.jfire.core.prepare.annotation.Import;
import com.jfireframework.jfire.core.prepare.annotation.condition.Conditional;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Bean;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;

import javax.annotation.Resource;

@Configuration
@ComponentScan("com.jfireframework.context.test.function.beanannotest")
@MyImport(name = "myimport")
@Import({HouseProvider.class, Data.NameProperty.class})
public class Data
{
    public static class NameProperty implements JfirePrepare
    {

        @Override
        public boolean prepare(JfireContext context)
        {
            context.getEnv().putProperty("name", "linbin");
            return true;
        }

        @Override
        public int order()
        {
            return 0;
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
