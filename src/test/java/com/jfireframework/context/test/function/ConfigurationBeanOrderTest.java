package com.jfireframework.context.test.function;

import java.lang.annotation.Annotation;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.condition.Condition;
import com.jfireframework.jfire.condition.Conditional;
import com.jfireframework.jfire.condition.provide.ConditionOnBean;
import com.jfireframework.jfire.config.annotation.Bean;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.config.annotation.Order;
import com.jfireframework.jfire.config.environment.Environment.ReadOnlyEnvironment;

public class ConfigurationBeanOrderTest
{
    
    public static class AlwaysTrue implements Condition
    {
        
        @Override
        public boolean match(ReadOnlyEnvironment readOnlyEnvironment, Annotation[] annotations)
        {
            return true;
        }
        
    }
    
    @Configuration
    public static class Order1
    {
        
        @Order(2)
        @Bean
        @Conditional(AlwaysTrue.class)
        public User1 user1()
        {
            return new User1();
        }
        
        @Bean
        @Order(1)
        @ConditionOnBean(User1.class)
        public User2 user2()
        {
            return new User2();
        }
    }
    
    @Configuration
    public static class Order2
    {
        
        @Order(1)
        @Bean
        @Conditional(AlwaysTrue.class)
        public User1 user1()
        {
            return new User1();
        }
        
        @Bean
        @Order(2)
        @ConditionOnBean(User1.class)
        public User2 user2()
        {
            return new User2();
        }
    }
    
    public static class User1
    {
        
    }
    
    public static class User2
    {
        
    }
    
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerConfiurationBeanDefinition(Order1.class);
        Jfire jfire = new Jfire(jfireConfig);
        User1 user1 = jfire.getBean(User1.class);
        Assert.assertNotNull(user1);
        User2 user2 = jfire.getBean(User2.class);
        Assert.assertNull(user2);
    }
    
    @Test
    public void test_2()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerConfiurationBeanDefinition(Order2.class);
        Jfire jfire = new Jfire(jfireConfig);
        User1 user1 = jfire.getBean(User1.class);
        Assert.assertNotNull(user1);
        User2 user2 = jfire.getBean(User2.class);
        Assert.assertNotNull(user2);
    }
}
