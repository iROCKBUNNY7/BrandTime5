package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.jfire.support.JfirePrepared.configuration.Bean;
import com.jfireframework.jfire.support.JfirePrepared.configuration.Configuration;

@Configuration
public class HouseProvider
{
    @Bean
    public House house2()
    {
        House house = new House();
        house.setName("house2");
        return house;
    }
}
