package com.jfireframework.jfire.support.BeanInstanceResolver.extend.bean.field.dependency;

import java.util.Map;

public interface DIField
{
    /**
     * 注入依赖实例
     * 
     * @param src 被注入的对象
     * @param beanInstanceMap bean实例map。key为bean的名称
     */
    void inject(Object src, Map<String, Object> beanInstanceMap);
    
    DiResolver diResolver();
    
    /**
     * 返回属性的名称
     * 
     * @return
     */
    String name();
}
