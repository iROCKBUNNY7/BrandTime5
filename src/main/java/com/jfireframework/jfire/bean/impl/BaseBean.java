package com.jfireframework.jfire.bean.impl;

import java.util.HashMap;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.jfire.bean.Bean;

public abstract class BaseBean extends BeanInitProcessImpl implements Bean
{
    /* bean对象初始化过程中暂存生成的中间对象 */
    protected final ThreadLocal<HashMap<String, Object>> beanInstanceMap = new ThreadLocal<HashMap<String, Object>>() {
                                                                             @Override
                                                                             protected HashMap<String, Object> initialValue()
                                                                             {
                                                                                 return new HashMap<String, Object>();
                                                                             }
                                                                         };
    /** 单例的引用对象 */
    protected Object                                     singletonInstance;
    
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
