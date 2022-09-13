package com.jfireframework.context.test.function;

import java.lang.annotation.Annotation;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.kernel.Jfire;
import com.jfireframework.jfire.kernel.Order;
import com.jfireframework.jfire.kernel.Environment.ReadOnlyEnvironment;
import com.jfireframework.jfire.support.JfirePrepared.Configuration;
import com.jfireframework.jfire.support.JfirePrepared.Configuration.Bean;
import com.jfireframework.jfire.support.JfirePrepared.condition.Condition;
import com.jfireframework.jfire.support.JfirePrepared.condition.Conditional;
import com.jfireframework.jfire.support.JfirePrepared.condition.provide.ConditionOnBean;

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
        jfireConfig.registerBeanDefinition(Order1.class);
        Jfire jfire = jfireConfig.build();
        User1 user1 = jfire.getBean(User1.class);
        Assert.assertNotNull(user1);
        User2 user2 = jfire.getBean(User2.class);
        Assert.assertNull(user2);
    }
    
    @Test
    public void test_2()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(Order2.class);
        Jfire jfire = jfireConfig.build();
        User1 user1 = jfire.getBean(User1.class);
        Assert.assertNotNull(user1);
        User2 user2 = jfire.getBean(User2.class);
        Assert.assertNotNull(user2);
    }
}
