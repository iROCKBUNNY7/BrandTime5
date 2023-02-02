package com.jfireframework.jfire.core.aop.impl.transaction;

public class Propagation
{
    /**
     * 支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择。
     */
    public static final int REQUIRED = 1;
    /**
     * 支持当前事务，如果当前没有事务，就以非事务方式执行。
     */
    public static final int SUPPORTS = 2;
    /**
     * 支持当前事务，如果当前没有事务，就抛出异常
     */
    public static final int MANDATORY = 3;
    /**
     * 新建事务，如果当前存在事务，把当前事务挂起
     */
    public static final int REQUIRES_NEW = 4;
}
