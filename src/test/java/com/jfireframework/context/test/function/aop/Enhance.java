package com.jfireframework.context.test.function.aop;

import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.aop.ProceedPoint;
import com.jfireframework.jfire.aop.annotation.AfterEnhance;
import com.jfireframework.jfire.aop.annotation.AroundEnhance;
import com.jfireframework.jfire.aop.annotation.BeforeEnhance;
import com.jfireframework.jfire.aop.annotation.EnhanceClass;
import com.jfireframework.jfire.aop.annotation.ThrowEnhance;

@EnhanceClass("com.jfireframework.context.*.aop.Person")
public class Enhance
{
    @BeforeEnhance("sayHello(String)")
    public void sayHello(ProceedPoint point)
    {
        System.out.println("前置拦截");
    }
    
    @BeforeEnhance("testInts(int[])")
    public void test(ProceedPoint point)
    {
        System.out.println("前置拦截2");
    }
    
    @BeforeEnhance(value = "order()", order = 2)
    public void order3(ProceedPoint point)
    {
        System.out.println("前置拦截3");
    }
    
    @BeforeEnhance(value = "order()")
    public void order2(ProceedPoint point)
    {
        System.out.println("前置拦截4");
    }
    
    @AfterEnhance("order2(String int)")
    public void order22(ProceedPoint point)
    {
        System.out.println("后置拦截");
    }
    
    @AroundEnhance("myName(String)")
    public String testMyname(ProceedPoint point)
    {
        System.out.println("环绕增强钱");
        String result = (String) point.invoke();
        System.out.println("环绕增强后");
        return result;
    }
    
    @BeforeEnhance(value = "*(*)", order = 10)
    public void all(ProceedPoint point)
    {
        System.out.println("所有方法均会输出");
    }
    
    @ThrowEnhance()
    public void throwe(ProceedPoint point)
    {
        System.out.println("dada");
        Verify.equal("aaaa", point.getE().getMessage(), "捕获到正确的异常");
    }
}
