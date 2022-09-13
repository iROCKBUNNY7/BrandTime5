package com.jfireframework.context.test.function.aop;

import org.junit.Assert;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.ProceedPoint;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.AfterEnhance;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.AroundEnhance;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.BeforeEnhance;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.EnhanceClass;
import com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect.annotation.ThrowEnhance;

@EnhanceClass("com.jfireframework.context.*.aop.Person")
public class Enhance
{
    private int    order;
    private String param;
    
    public int getOrder()
    {
        return order;
    }
    
    public String getParam()
    {
        return param;
    }
    
    @BeforeEnhance("sayHello(String)")
    public void sayHello(ProceedPoint point)
    {
        param = (String) point.getParams()[0];
        System.out.println("前置拦截");
    }
    
    @AroundEnhance("testInts(int[])")
    public String[] test(ProceedPoint point)
    {
        System.out.println("asdasd");
        String[] origin = (String[]) point.invoke();
        Assert.assertEquals(3, origin.length);
        return new String[0];
    }
    
    @BeforeEnhance(value = "order()", order = 2)
    public void order3(ProceedPoint point)
    {
        order = 3;
        System.out.println("触发");
    }
    
    @BeforeEnhance(value = "order()")
    public void order2(ProceedPoint point)
    {
        order = 4;
    }
    
    private Object result;
    
    public Object getResult()
    {
        return result;
    }
    
    @AfterEnhance("order2(String int)")
    public void order22(ProceedPoint point)
    {
        System.out.println("后置拦截");
        result = point.getResult();
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
