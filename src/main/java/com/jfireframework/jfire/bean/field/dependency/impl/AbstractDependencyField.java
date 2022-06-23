package com.jfireframework.jfire.bean.field.dependency.impl;

import java.lang.reflect.Field;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.simplelog.ConsoleLogFactory;
import com.jfireframework.baseutil.simplelog.Logger;
import com.jfireframework.jfire.bean.field.dependency.DIField;
import sun.misc.Unsafe;

public abstract class AbstractDependencyField implements DIField
{
    protected final long          offset;
    protected static Unsafe       unsafe = ReflectUtil.getUnsafe();
    protected final static Logger logger = ConsoleLogFactory.getLogger();
    protected final Field         field;
    
    public AbstractDependencyField(Field field)
    {
        this.offset = unsafe.objectFieldOffset(field);
        this.field = field;
        
    }
    
}
