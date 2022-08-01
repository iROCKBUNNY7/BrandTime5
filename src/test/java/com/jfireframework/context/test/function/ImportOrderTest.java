package com.jfireframework.context.test.function;

import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.jfire.Jfire;
import com.jfireframework.jfire.JfireConfig;
import com.jfireframework.jfire.bean.annotation.Order;
import com.jfireframework.jfire.bean.annotation.field.PropertyRead;
import com.jfireframework.jfire.config.annotation.Configuration;
import com.jfireframework.jfire.config.annotation.Import;
import com.jfireframework.jfire.config.environment.Environment;
import com.jfireframework.jfire.importer.ImportSelecter;

public class ImportOrderTest
{
    /**
     * 测试Order注解在ImportSelecter上是否生效
     */
    @Test
    public void test()
    {
        JfireConfig jfireConfig = new JfireConfig();
        jfireConfig.registerBeanDefinition(Order1.class);
        Jfire jfire = new Jfire(jfireConfig);
        Order1 order1 = jfire.getBean(Order1.class);
        Assert.assertEquals("1,2", order1.getResult());
    }
    
    @Resource
    @Configuration
    @Import({ Import2.class, Import1.class })
    public static class Order1
    {
        @PropertyRead("result")
        private String result;
        
        public String getResult()
        {
            return result;
        }
        
        public void setResult(String result)
        {
            this.result = result;
        }
        
    }
    
    @Order(1)
    public static class Import1 implements ImportSelecter
    {
        
        @Override
        public void importSelect(Environment environment)
        {
            String result = environment.getProperty("result");
            if (result == null)
            {
                result = "1";
            }
            else
            {
                result = result + ",1";
            }
            environment.putProperty("result", result);
        }
        
    }
    
    @Order(2)
    public static class Import2 implements ImportSelecter
    {
        
        @Override
        public void importSelect(Environment environment)
        {
            String result = environment.getProperty("result");
            if (result == null)
            {
                result = "2";
            }
            else
            {
                result = result + ",2";
            }
            environment.putProperty("result", result);
        }
        
    }
}
