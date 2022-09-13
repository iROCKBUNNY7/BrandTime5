package com.jfireframework.context.test.function.initmethod;

import javax.annotation.Resource;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.ProceedPoint;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.BeforeEnhance;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.EnhanceClass;

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
