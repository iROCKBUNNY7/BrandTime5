package com.jfireframework.jfire.exception;

import com.jfireframework.baseutil.StringUtil;

public class MapKeyMethodCanNotFindException extends RuntimeException
{

    /**
     *
     */
    private static final long serialVersionUID = 3922510410797583655L;

    public MapKeyMethodCanNotFindException(String methodName, Class<?> type, Throwable e)
    {
        super(StringUtil.format("无法在类:{}找到方法:{}", type.getName(), methodName), e);
    }
}
