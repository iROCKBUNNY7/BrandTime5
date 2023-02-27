package com.jfireframework.jfire.exception;

public class DuplicateBeanNameException extends RuntimeException
{

    /**
     *
     */
    private static final long serialVersionUID = 7055678999269659122L;

    public DuplicateBeanNameException(String beanName)
    {
        super("bean名称:" + beanName + "重复");
    }
}
