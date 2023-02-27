package com.jfireframework.context.test.function.aop;

import com.jfireframework.jfire.core.aop.impl.transaction.ConnectionHolder;
import com.jfireframework.jfire.core.aop.impl.transaction.JdbcTransactionManager;
import com.jfireframework.jfire.core.aop.impl.transaction.TransactionState;

import javax.annotation.Resource;

@Resource
public class TxManager extends JdbcTransactionManager
{
    private boolean beginTransAction;
    private boolean commit;

    public boolean isBeginTransAction()
    {
        return beginTransAction;
    }

    public boolean isCommit()
    {
        return commit;
    }

    @Override
    public TransactionState beginTransAction(int propagation)
    {
        beginTransAction = true;
        return super.beginTransAction(propagation);
    }

    @Override
    public void commit(TransactionState transaction)
    {
        commit = true;
        super.commit(transaction);
    }

    @Override
    public void rollback(TransactionState transaction, Throwable e)
    {
        System.out.println("事务回滚");
        super.rollback(transaction, e);
    }

    @Override
    protected ConnectionHolder openConnection()
    {
        return new ConnectionHolder()
        {
            private boolean transactionActive;
            private boolean closed = false;

            @Override
            public void setPrev(ConnectionHolder connectionHolder)
            {
                // TODO Auto-generated method stub
            }

            @Override
            public void rollback()
            {
                // TODO Auto-generated method stub
            }

            @Override
            public boolean isTransactionActive()
            {
                return transactionActive;
            }

            @Override
            public boolean isClosed()
            {
                return closed;
            }

            @Override
            public ConnectionHolder getPrev()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void commit()
            {
            }

            @Override
            public void close()
            {
                closed = true;
            }

            @Override
            public void beginTransaction()
            {
                transactionActive = true;
            }
        };
    }
}
