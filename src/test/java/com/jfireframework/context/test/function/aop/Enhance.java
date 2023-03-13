package com.jfireframework.context.test.function.aop;

import com.jfireframework.baseutil.Verify;
import com.jfireframework.jfire.core.aop.ProceedPoint;
import com.jfireframework.jfire.core.aop.notated.*;
import org.junit.Assert;

@EnhanceClass("com.jfireframework.context.*.aop.Person")
public class Enhance
{

    @EnhanceClass(value = "com.jfireframework.context.*.aop.Person", order = 2)
    public static class EnhanceForOrder
    {
        public static String result = "";

        @Before(value = "order()")
        public void order2(ProceedPoint point)
        {
            result += "EnhanceForOrder_";
        }
    }

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

    @Before("sayHello(String)")
    public void sayHello(ProceedPoint point)
    {
        param = (String) point.getParams()[0];
        System.out.println("前置拦截");
    }

    @Around("testInts(int[])")
    public void test(ProceedPoint point)
    {
        System.out.println("asdasd");
        point.invoke();
        Assert.assertEquals(((int[]) point.getParams()[0]).length, ((String[]) point.getResult()).length);
    }

    @Before(value = "order()")
    public void order2(ProceedPoint point)
    {
        EnhanceForOrder.result += "enhance";
    }

    private Object result;

    public Object getResult()
    {
        return result;
    }

    @AfterReturning("order2(String,int)")
    public void order22(ProceedPoint point)
    {
        result = point.getResult();
    }

    @Around("myName(String)")
    public void testMyname(ProceedPoint point)
    {
        System.out.println("环绕增强钱");
        point.invoke();
        System.out.println("环绕增强后");
    }

    @Around("testForVoidReturn()")
    public void testForVoidReturn(ProceedPoint point)
    {
        System.out.println("环绕增强钱");
        point.invoke();
        point.invoke();
        System.out.println("环绕增强后");
    }

    @AfterThrowable("throwe(*)")
    public void throwe(ProceedPoint point)
    {
        System.out.println("dada");
        Verify.equal("aaaa", point.getE().getMessage(), "捕获到正确的异常");
    }
}
