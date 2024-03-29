package com.jfirer.jfire.core.aop;

/**
 * 方法连接点的抽象表示
 *
 * @author 林斌
 */
public interface ProceedPoint
{
    class MethodDescription
    {
        final String methodName;
        Class<?>[] paramTypes;

        public MethodDescription(String methodName, Class<?>[] paramTypes)
        {
            this.methodName = methodName;
            this.paramTypes = paramTypes;
        }

        String methodName()
        {
            return methodName;
        }

        Class<?>[] getParamTypes()
        {
            return paramTypes;
        }
    }

    /**
     * 表示对目标方法的调用。在静态代码中作为继承方法被修改以实现对目标方法的调用
     *
     * @return
     */
    void invoke();

    /**
     * 返回目标方法的调用对象实例
     *
     * @return
     */
    Object getHost();

    /**
     * 被拦截的方法
     *
     * @return
     */
    MethodDescription getMethod();

    /**
     * 在异常增强中，返回原方法抛出的异常
     *
     * @return
     */
    Throwable getE();

    /**
     * 在后置增强中，返回原方法的执行结果。其余方法无效。
     *
     * @return
     */
    Object getResult();

    /**
     * 获得目标方法的入参数组
     *
     * @return
     */
    Object[] getParams();
}
