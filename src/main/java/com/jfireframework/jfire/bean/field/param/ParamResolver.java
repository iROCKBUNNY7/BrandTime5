package com.jfireframework.jfire.bean.field.param;

import java.lang.reflect.Field;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import sun.misc.Unsafe;

public interface ParamResolver
{
    public static final Unsafe unsafe = ReflectUtil.getUnsafe();
    
    /**
     * 注入数据时调用该接口
     * 
     * @param originValue
     * @param entity
     * @param field
     * @param offset 如果不能提供偏移量数据时，该参数为-1
     */
    void setValue(Object entity);
    
    void initialize(String value, Field field);
}
