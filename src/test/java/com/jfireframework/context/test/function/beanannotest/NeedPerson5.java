package com.jfireframework.context.test.function.beanannotest;

import javax.annotation.Resource;

@Resource
public class NeedPerson5
{
    @Resource(name = "person5")
    private Person person;

    public Person getPerson()
    {
        return person;
    }
}
