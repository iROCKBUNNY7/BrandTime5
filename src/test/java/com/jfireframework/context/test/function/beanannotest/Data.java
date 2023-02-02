package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.context.test.function.beanannotest.Data.NameProperty;
import com.jfireframework.jfire.core.inject.notated.PropertyRead;
import com.jfireframework.jfire.core.prepare.JfirePrepare;
import com.jfireframework.jfire.core.prepare.JfirePreparedNotated;
import com.jfireframework.jfire.core.prepare.condition.Conditional;
import com.jfireframework.jfire.core.prepare.impl.ComponentScan;
import com.jfireframework.jfire.core.prepare.impl.Configuration;
import com.jfireframework.jfire.core.prepare.impl.Configuration.Bean;
import com.jfireframework.jfire.core.prepare.impl.Import;

import javax.annotation.Resource;

@Configuration
@ComponentScan("com.jfireframework.context.test.function.beanannotest")
@MyImport(name = "myimport")
@Import({HouseProvider.class, NameProperty.class})
@Resource
public class Data
{
    @JfirePreparedNotated
    public static class NameProperty implements JfirePrepare
    {

        @Override
        public void prepare(com.jfireframework.jfire.core.Environment environment)
        {
            environment.putProperty("name", "linbin");
//			environment.putProperty("person2", "pass");
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
