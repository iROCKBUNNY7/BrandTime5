package com.jfirer.jfire.test.function;

import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.core.prepare.annotation.configuration.Bean;
import com.jfirer.jfire.core.prepare.annotation.configuration.ConfigAfter;
import com.jfirer.jfire.core.prepare.annotation.configuration.ConfigBefore;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

@ComponentScan("com.jfirer.jfire.test.function:in~com.jfirer.jfire.test.function.ConfigurationOrderTest*")
public class ConfigurationOrderTest
{
    public static AtomicInteger count = new AtomicInteger();

    public static class Person
    {
        private int age = -1;

        public Person(int age)
        {
            this.age = age;
        }

        public int getAge()
        {
            return age;
        }
    }

    @Configuration
    public static class Order_1
    {
        @Bean
        public Person person()
        {
            if (count.get() == 0)
            {
                count.set(1);
                return new Person(1);
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    @Configuration
    public static class Order_3
    {
        @Bean
        public Person person2()
        {
            if (count.get() == 1)
            {
                count.set(2);
                return new Person(2);
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    @Configuration
    @ConfigBefore(Order_3.class)
    @ConfigAfter(Order_1.class)
    public static class Order_2
    {
        @Bean
        public static Person person3()
        {
            if (count.get() == 2)
            {
                count.set(3);
                return new Person(3);
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    @Configuration
    @ConfigAfter(Order_3.class)
    @ConfigBefore(Order_5.class)
    public static class Order_4
    {
        @Bean
        public static Person person4()
        {
            if (count.get() == 3)
            {
                count.set(4);
                return new Person(4);
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    @Configuration
    public static class Order_5
    {
        @Bean
        public static Person person5()
        {
            if (count.get() == 4)
            {
                count.set(5);
                return new Person(5);
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext(ConfigurationOrderTest.class);
        Assert.assertEquals(1, ((Person) context.getBean("person")).getAge());
        Assert.assertEquals(2, ((Person) context.getBean("person2")).getAge());
        Assert.assertEquals(3, ((Person) context.getBean("person3")).getAge());
        Assert.assertEquals(4, ((Person) context.getBean("person4")).getAge());
        Assert.assertEquals(5, ((Person) context.getBean("person5")).getAge());
        Assert.assertEquals(5, count.get());
    }
}
