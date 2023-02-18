package com.jfireframework.context.test.function;

import com.jfireframework.jfire.core.Jfire;
import com.jfireframework.jfire.core.JfireBootstrap;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Bean;
import com.jfireframework.jfire.core.prepare.annotation.configuration.ConfigAfter;
import com.jfireframework.jfire.core.prepare.annotation.configuration.ConfigBefore;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigurationOrderTest<sta>
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

    @Resource
    @Configuration
    public static class Order_1
    {
        @Bean
        public Person person()
        {
            if ( count.get() == 0 )
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

    @Resource
    @Configuration
    public static class Order_3
    {
        @Bean
        public Person person2()
        {
            if ( count.get() == 1 )
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

    @Resource
    @Configuration
    @ConfigBefore(Order_3.class)
    @ConfigAfter(Order_1.class)
    public static class Order_2
    {
        @Bean
        public static Person person3()
        {
            if ( count.get() == 2 )
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
    @Resource
    @ConfigAfter(Order_3.class)
    @ConfigBefore(Order_5.class)
    public static class Order_4
    {
        @Bean
        public static Person person4()
        {
            if ( count.get() == 3 )
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
    @Resource
    public static class Order_5
    {
        @Bean
        public static Person person5()
        {
            if ( count.get() == 4 )
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
        JfireBootstrap bootstrap = new JfireBootstrap();
        bootstrap.register(Order_1.class);
        bootstrap.register(Order_2.class);
        bootstrap.register(Order_3.class);
        bootstrap.register(Order_4.class);
        bootstrap.register(Order_5.class);
        Jfire jfire = bootstrap.start();
        Assert.assertEquals(1, ((Person) jfire.getBean("person")).getAge());
        Assert.assertEquals(2, ((Person) jfire.getBean("person2")).getAge());
        Assert.assertEquals(3, ((Person) jfire.getBean("person3")).getAge());
        Assert.assertEquals(4, ((Person) jfire.getBean("person4")).getAge());
        Assert.assertEquals(5, ((Person) jfire.getBean("person5")).getAge());
        Assert.assertEquals(5, count.get());
    }
}
