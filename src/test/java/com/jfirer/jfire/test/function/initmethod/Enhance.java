package com.jfirer.jfire.test.function.initmethod;

import com.jfirer.jfire.core.aop.ProceedPoint;
import com.jfirer.jfire.core.aop.notated.Before;
import com.jfirer.jfire.core.aop.notated.EnhanceClass;

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
