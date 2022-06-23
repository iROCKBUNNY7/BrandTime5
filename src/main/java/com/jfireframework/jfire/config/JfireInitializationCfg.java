package com.jfireframework.jfire.config;

import java.util.HashMap;
import com.jfireframework.jfire.bean.BeanDefinition;

public class JfireInitializationCfg
{
    private BeanDefinition[]        beanDefinitions  = new BeanDefinition[0];
    private String[]                scanPackageNames = new String[0];
    private String[]                propertyPaths    = new String[0];
    private HashMap<String, String> properties       = new HashMap<String, String>();
    
    public BeanDefinition[] getBeanDefinitions()
    {
        return beanDefinitions;
    }
    
    public void setBeanDefinitions(BeanDefinition[] beanDefinitions)
    {
        this.beanDefinitions = beanDefinitions;
    }
    
    public String[] getScanPackageNames()
    {
        return scanPackageNames;
    }
    
    public void setScanPackageNames(String... scanPackageNames)
    {
        this.scanPackageNames = scanPackageNames;
    }
    
    public String[] getPropertyPaths()
    {
        return propertyPaths;
    }
    
    public void setPropertyPaths(String... propertyPaths)
    {
        this.propertyPaths = propertyPaths;
    }
    
    public HashMap<String, String> getProperties()
    {
        return properties;
    }
    
    public void setProperties(HashMap<String, String> properties)
    {
        this.properties = properties;
    }
    
}
