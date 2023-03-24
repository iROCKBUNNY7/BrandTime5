package com.jfireframework.context.test.function.initmethod;

import com.jfireframework.jfire.core.aop.ProceedPoint;
import com.jfireframework.jfire.core.aop.notated.Before;
import com.jfireframework.jfire.core.aop.notated.EnhanceClass;

import javax.annotation.Resource;

@Resource
@EnhanceClass("com.jfire.*.start*")
public class Enhance
{
    @Before("initage()")
    public void initage(ProceedPoint point)
    {
        System.out.println("dads");
    }
}
