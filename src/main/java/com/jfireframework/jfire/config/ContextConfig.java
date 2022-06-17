package com.jfireframework.jfire.config;

import java.util.HashMap;

public class ContextConfig
{
    private String[]                packageNames  = new String[0];
    private BeanInfo[]              beans         = new BeanInfo[0];
    private String[]                propertyPaths = new String[0];
    private Profile[]               profiles      = new Profile[0];
    private HashMap<String, String> properties    = new HashMap<String, String>();
    private String                  activeProfile;
    
    public String getActiveProfile()
    {
        return activeProfile;
    }
    
    public void setActiveProfile(String activeProfile)
    {
        this.activeProfile = activeProfile;
    }
    
    public String[] getPropertyPaths()
    {
        return propertyPaths;
    }
    
    public void setPropertyPaths(String[] propertyPaths)
    {
        this.propertyPaths = propertyPaths;
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
    
    public Profile[] getProfiles()
    {
        return profiles;
    }
    
    public void setProfiles(Profile[] profiles)
    {
        this.profiles = profiles;
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
