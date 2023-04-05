package com.jfirer.jfire.core.aop.impl.transaction;

public interface TransactionState
{

    /**
     * 返回该事务状态的传播级别
     *
     * @return
     */
    int propagation();

    /**
     * 当前事务上下文是否已经完成
     *
     * @return
     */
    boolean isContextCompleted();

    /**
     * 开启当前事务状态时是否打开了一个新的连接
     *
     * @return
     */
    boolean isBeginWithNewConnection();
}
