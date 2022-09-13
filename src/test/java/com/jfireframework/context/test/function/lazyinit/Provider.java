package com.jfireframework.context.test.function.lazyinit;

import com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.annotation.LazyInitUniltFirstInvoke;
import com.jfireframework.jfire.support.JfirePrepared.Configuration;
import com.jfireframework.jfire.support.JfirePrepared.Configuration.Bean;

@Configuration
public class Provider
{
    @Bean
    @LazyInitUniltFirstInvoke
    public OriginInstance2 originInstance2()
    {
        LazyInitTest.invokedCount2 += 1;
        return new OriginInstance2();
    }
    
    @Bean(prototype = true)
    @LazyInitUniltFirstInvoke
    public OriginInstance3 originInstance3()
    {
        LazyInitTest.invokedCount3 += 1;
        return new OriginInstance3();
    }
}
