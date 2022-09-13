package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.aspect;

/**
 * 方法连接点的抽象表示
 * 
 * @author 林斌
 * 
 */
public interface ProceedPoint
{
    
    /**
     * 表示对目标方法的调用。在静态代码中作为继承方法被修改以实现对目标方法的调用
     * 
     * @return
     */
    public Object invoke();
    
    /**
     * 返回目标方法的调用对象实例
     * 
     * @return
     */
    public Object getHost();
    
    /**
     * 在异常增强中，返回原方法抛出的异常
     * 
     * @return
     */
    public Throwable getE();
    
    /**
     * 在后置增强中，返回原方法的执行结果。其余方法无效。
     * 
     * @return
     */
    public Object getResult();
    
    /**
     * 获得目标方法的入参数组
     * 
     * @return
     */
    public Object[] getParams();
    
}
