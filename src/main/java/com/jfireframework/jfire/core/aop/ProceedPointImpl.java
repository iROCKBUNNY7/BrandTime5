package com.jfireframework.jfire.core.aop;

import java.lang.reflect.Method;

/**
 * 用于在AOP增强中对连接点的抽象。
 * 
 * @author linbin
 * 
 */
public class ProceedPointImpl implements ProceedPoint
{
    // 目标类对象实例
    protected Object    host;
    // 目标类执行后的返回结果实例
    protected Object    result;
    // 目标方法抛出的异常
    protected Throwable e;
    // 目标方法的参数数组
    protected Object[]  params;
    protected Method    method;
    
    /**
     * 表示对目标方法的调用。在静态代码中作为继承方法被修改以实现对目标方法的调用
     * 
     * @return
     */
    @Override
    public Object invoke()
    {
        throw new RuntimeException("该方法只在环绕增强方法中可被调用，其余情况均异常");
    }
    
    @Override
    public Object getHost()
    {
        return host;
    }
    
    public void setHost(Object host)
    {
        this.host = host;
    }
    
    @Override
    public Throwable getE()
    {
        return e;
    }
    
    public void setE(Throwable e)
    {
        this.e = e;
    }
    
    @Override
    public Object getResult()
    {
        return result;
    }
    
    public void setResult(Object result)
    {
        this.result = result;
    }
    
    @Override
    public Object[] getParams()
    {
        return params;
    }
    
    public void setParams(Object... params)
    {
        this.params = params;
    }
    
    @Override
    public Method getMethod()
    {
        return method;
    }
    
    public void setMethod(Method method)
    {
        this.method = method;
    }
    
}
