package com.jfireframework.context.test.function.initmethod;

import javax.annotation.Resource;
import com.jfireframework.jfire.core.aop.ProceedPoint;
import com.jfireframework.jfire.core.aop.notated.EnhanceClass;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.BeforeEnhance;

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
