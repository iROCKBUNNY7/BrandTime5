package com.jfireframework.jfire.bean.field.param.impl;

import java.io.File;

public class FileResolver extends ObjectResolver
{
    
    @Override
    protected void initialize(String value)
    {
        this.value = new File(value);
    }
    
}