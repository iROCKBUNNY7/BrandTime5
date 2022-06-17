package com.jfireframework.context.test.function.initmethod;

import javax.annotation.Resource;
import com.jfireframework.jfire.aop.ProceedPoint;
import com.jfireframework.jfire.aop.annotation.BeforeEnhance;
import com.jfireframework.jfire.aop.annotation.EnhanceClass;

@Resource
@EnhanceClass("com.jfire.*.init*")
public class Enhance
{
    @BeforeEnhance("initage()")
    public void initage(ProceedPoint point)
    {
        System.out.println("dads");
    }
}
