package com.jfireframework.jfire.bean.impl;

import java.util.Map;
import com.jfireframework.jfire.bean.Bean;
import com.jfireframework.jfire.bean.load.BeanLoadFactory;

public class LoadByBean extends BaseBean
{
    
    private final Bean loadByFactoryBean;
    
    public LoadByBean(Class<?> type, String beanName, Bean loadByFactoryBean)
    {
        super(type, beanName, false, null, null);
        this.loadByFactoryBean = loadByFactoryBean;
    }
    
    @Override
    public Object getInstance()
    {
        BeanLoadFactory factory = (BeanLoadFactory) loadByFactoryBean.getInstance();
        return factory.load(type);
    }
    
    @Override
    public Object getInstance(Map<String, Object> beanInstanceMap)
    {
        if (beanInstanceMap.containsKey(beanName))
        {
            return beanInstanceMap.get(beanName);
        }
        else
        {
            BeanLoadFactory factory = (BeanLoadFactory) loadByFactoryBean.getInstance(beanInstanceMap);
            Object entity = factory.load(type);
            beanInstanceMap.put(beanName, entity);
            return entity;
        }
    }
    
}
