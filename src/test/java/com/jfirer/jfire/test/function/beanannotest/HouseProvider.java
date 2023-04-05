package com.jfirer.jfire.test.function.beanannotest;

import com.jfirer.jfire.core.prepare.annotation.configuration.Bean;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;

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
