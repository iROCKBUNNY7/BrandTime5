package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.jfire.config.annotation.Bean;
import com.jfireframework.jfire.config.annotation.Configuration;

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
