package com.jfirer.jfire.test.function.base.data;

import com.jfirer.jfire.core.inject.notated.PropertyRead;

public class PropertyReadData
{
    @PropertyRead("inner.age")
    private int age;
    @PropertyRead("age12")
    private int age1 = 10;

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    public int getAge1()
    {
        return age1;
    }

    public void setAge1(int age1)
    {
        this.age1 = age1;
    }
}
