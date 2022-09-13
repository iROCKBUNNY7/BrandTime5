package com.jfireframework.jfire.kernel;

public class BeanDefinition
{
    private final String               beanName;
    private final Class<?>             originType;
    private final boolean              prototype;
    private final BeanInstanceResolver beanInstanceResolver;
    
    public BeanDefinition(String beanName, Class<?> originType, boolean prototype, BeanInstanceResolver beanInstanceResolver)
    {
        this.beanName = beanName;
        this.originType = originType;
        this.prototype = prototype;
        this.beanInstanceResolver = beanInstanceResolver;
    }
    
    public BeanInstanceResolver getBeanInstanceResolver()
    {
        return beanInstanceResolver;
    }
    
    public String getBeanName()
    {
        return beanName;
    }
    
    public Class<?> getOriginType()
    {
        return originType;
    }
    
    public boolean isPrototype()
    {
        return prototype;
    }
    
}
