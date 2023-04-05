package com.jfirer.jfire.exception;

public class ConditionCannotInstanceException extends RuntimeException
{

    /**
     *
     */
    private static final long serialVersionUID = -4840737369883745113L;

    public ConditionCannotInstanceException(Class<?> ckass, Throwable e)
    {
        super("无法实例化条件类:" + ckass.getName(), e);
    }
}
