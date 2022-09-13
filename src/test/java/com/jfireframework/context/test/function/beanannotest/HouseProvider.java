package com.jfireframework.context.test.function.beanannotest;

import com.jfireframework.jfire.support.jfireprepared.Configuration;
import com.jfireframework.jfire.support.jfireprepared.Configuration.Bean;

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
