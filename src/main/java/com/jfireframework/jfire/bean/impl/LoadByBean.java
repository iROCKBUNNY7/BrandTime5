package com.jfireframework.jfire.bean.impl;

import java.util.Map;
import com.jfireframework.jfire.bean.Bean;
import com.jfireframework.jfire.bean.load.BeanLoadFactory;

public class LoadByBean extends BaseBean
{
    
    private final Bean loadByFactoryBean;
    
    public LoadByBean(Class<?> type, boolean prototype, String beanName, Bean loadByFactoryBean, boolean lazyInitUntilFirstInvoke)
    {
        super(type, beanName, prototype, lazyInitUntilFirstInvoke);
        this.loadByFactoryBean = loadByFactoryBean;
    }
    
    @Override
    protected Object buildInstance(Map<String, Object> beanInstanceMap)
    {
        BeanLoadFactory factory = (BeanLoadFactory) loadByFactoryBean.getInstance(beanInstanceMap);
        Object entity = factory.load(type);
        beanInstanceMap.put(beanName, entity);
        return entity;
    }
    
}
