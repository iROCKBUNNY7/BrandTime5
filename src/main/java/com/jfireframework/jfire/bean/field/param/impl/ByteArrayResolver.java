package com.jfireframework.jfire.bean.field.param.impl;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.encrypt.Base64Tool;

public class ByteArrayResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        if (value.startsWith("0x"))
        {
            this.value = StringUtil.hexStringToBytes(value.substring(2));
        }
        else
        {
            this.value = Base64Tool.decode(value);
        }
    }
    
}