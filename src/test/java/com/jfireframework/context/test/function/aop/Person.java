package com.jfireframework.context.test.function.aop;

import com.jfireframework.jfire.core.aop.impl.transaction.Propagation;
import com.jfireframework.jfire.core.aop.notated.Transactional;

import javax.annotation.Resource;

@Resource
public class Person
{
    private String name        = "林斌";
    private int    invokeCount = 0;

    public String sayHello(String word)
    {
        invokeCount++;
        return name + "说" + word;
    }

    @Resource(name = "注解保留")
    public void sayHello()
    {
        invokeCount++;
        throw new RuntimeException("自定义错误");
    }

    public String[] testInts(int[] ints)
    {
        invokeCount++;
        String[] strs = new String[ints.length];
        for (int i = 0; i < strs.length; i++)
        {
            strs[0] = String.valueOf(ints[i]);
        }
        return strs;
    }

    public String order()
    {
        invokeCount++;
        return "1";
    }

    public String order2(String name, int age)
    {
        invokeCount++;
        return name + age;
    }

    public String myName(String word)
    {
        invokeCount++;
        return name + word;
    }

    public void throwe()
    {
        invokeCount++;
        throw new RuntimeException("aaaa");
    }

    @Transactional
    public void tx()
    {
        invokeCount++;
        System.out.println("数据访问");
        hh();
    }

    protected void hh()
    {
        System.out.println("dsada");
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void autoClose()
    {
        invokeCount++;
        String name = "12";
        System.out.println("自动关闭");
    }

    public int invokeCount()
    {
        return invokeCount;
    }
}
