package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfireframework.jfire.core.prepare.annotation.configuration.Bean;

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
