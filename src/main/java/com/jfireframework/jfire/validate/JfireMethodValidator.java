package com.jfireframework.jfire.validate;

public interface JfireMethodValidator
{
    /**
     * 验证所有的方法参数
     * 
     * @param params
     * @param groups
     * @return
     */
    ValidateResult validateParams(Object[] params, Class<?>... groups);
    
    /**
     * 验证某一个参数
     * 
     * @param param
     * @param groups
     * @return
     */
    ValidateResult validateParam(Object param, Class<?>... groups);
}
