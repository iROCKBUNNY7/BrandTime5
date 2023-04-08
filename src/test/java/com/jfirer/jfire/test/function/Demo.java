package com.jfirer.jfire.test.function;

import java.lang.reflect.Method;

public class Demo
{
    public void say(){
        ;
    }
    protected  void say2(){
        ;
    }

    public static void main(String[] args)
    {
        for (Method each : Demo.class.getMethods())
        {
            System.out.println(each);
        }
    }
}
