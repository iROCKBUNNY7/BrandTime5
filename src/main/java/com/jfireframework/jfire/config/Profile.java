package com.jfireframework.jfire.config;

import java.util.HashMap;
import java.util.Map;

public class Profile
{
    private String              name;
    private String[]            packageNames  = new String[0];
    private BeanInfo[]          beans         = new BeanInfo[0];
    private String[]            propertyPaths = new String[0];
    private Map<String, String> properties    = new HashMap<String, String>();
    
    public Map<String, String> getProperties()
    {
        return properties;
    }
    
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String[] getPackageNames()
    {
        return packageNames;
    }
    
    public void setPackageNames(String[] packageNames)
    {
        this.packageNames = packageNames;
    }
    
    public BeanInfo[] getBeans()
    {
        return beans;
    }
    
    public void setBeans(BeanInfo[] beans)
    {
        this.beans = beans;
    }
    
    public String[] getPropertyPaths()
    {
        return propertyPaths;
    }
    
    public void setPropertyPaths(String[] propertyPaths)
    {
        this.propertyPaths = propertyPaths;
    }
    
}
