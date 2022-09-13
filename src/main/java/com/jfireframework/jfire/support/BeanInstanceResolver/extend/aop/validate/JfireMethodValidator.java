package com.jfireframework.jfire.support.BeanInstanceResolver.extend.aop.validate;

import java.lang.reflect.Method;

public interface JfireMethodValidator
{
    /**
     * 验证该方法的参数
     * 
     * @param method
     * @param params
     * @param groups
     * @return
     */
    ValidateResult validate(Method method, Object[] params, Class<?>... groups);
}
