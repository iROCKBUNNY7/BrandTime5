package com.jfireframework.jfire.bean.impl;

import java.util.HashMap;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.bean.Bean;
import com.jfireframework.jfire.bean.field.dependency.DIField;
import com.jfireframework.jfire.bean.field.param.ParamField;
import sun.reflect.MethodAccessor;

public abstract class BaseBean implements Bean
{
    protected MethodAccessor                                    postConstructMethod;
    protected MethodAccessor                                    preDestoryMethod;
    /* bean对象初始化过程中暂存生成的中间对象 */
    protected static final ThreadLocal<HashMap<String, Object>> beanInstanceMap = new ThreadLocal<HashMap<String, Object>>() {
                                                                                    @Override
                                                                                    protected HashMap<String, Object> initialValue()
                                                                                    {
                                                                                        return new HashMap<String, Object>();
                                                                                    }
                                                                                };
    /** 单例的引用对象 */
    protected volatile Object                                   singletonInstance;
    protected ParamField[]                                      paramFields;
    protected DIField[]                                         diFields;
    protected final boolean                                     prototype;
    protected final String                                      beanName;
    protected final Class<?>                                    type;
    
    public BaseBean(Class<?> type, String beanName, boolean prototype, DIField[] diFields, ParamField[] paramFields)
    {
        this.beanName = beanName;
        this.type = type;
        this.prototype = prototype;
        this.diFields = diFields;
        this.paramFields = paramFields;
    }
    
    public void setPostConstructMethod(MethodAccessor postConstructMethod)
    {
        this.postConstructMethod = postConstructMethod;
    }
    
    public void setPreDestoryMethod(MethodAccessor preDestoryMethod)
    {
        this.preDestoryMethod = preDestoryMethod;
    }
    
    public Class<?> getType()
    {
        return type;
    }
    
    public void close()
    {
        if (prototype == false && preDestoryMethod != null)
        {
            try
            {
                preDestoryMethod.invoke(singletonInstance, null);
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
        }
    }
}
